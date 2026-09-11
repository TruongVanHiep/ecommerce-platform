package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.SepayWebhookRequest;
import com.dev.E_commerce.Mini.dto.response.PaymentResponse;
import com.dev.E_commerce.Mini.entity.Order;
import com.dev.E_commerce.Mini.entity.Payment;
import com.dev.E_commerce.Mini.enums.PaymentMethod;
import com.dev.E_commerce.Mini.enums.PaymentStatus;
import com.dev.E_commerce.Mini.enums.Status;
import com.dev.E_commerce.Mini.repository.OrderRepository;
import com.dev.E_commerce.Mini.repository.PaymentRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Webhook SePay là endpoint public có quyền đánh dấu đơn đã thanh toán, nên
 * test tập trung vào các lý do nó phải TỪ CHỐI hoặc BỎ QUA — đó là chỗ một lỗi
 * nhỏ biến thành mất tiền thật.
 */
@ExtendWith(MockitoExtension.class)
class SepayServiceTest {

    private static final String API_KEY = "sepay-test-key";
    private static final String ACCOUNT = "0123456789";

    @Mock PaymentRepository paymentRepository;
    @Mock OrderRepository orderRepository;

    SepayService sepayService;

    @BeforeEach
    void setUp() {
        sepayService = new SepayService(paymentRepository, orderRepository, new SimpleMeterRegistry());
        // @Value không được xử lý khi khởi tạo thủ công nên gán tay.
        ReflectionTestUtils.setField(sepayService, "apiKey", API_KEY);
        ReflectionTestUtils.setField(sepayService, "bankCode", "MBBank");
        ReflectionTestUtils.setField(sepayService, "accountNumber", ACCOUNT);
        ReflectionTestUtils.setField(sepayService, "accountName", "NGUYEN VAN A");
        ReflectionTestUtils.setField(sepayService, "transferPrefix", "DH");
    }

    private SepayWebhookRequest webhook(long sepayId, String content, long amount) {
        return SepayWebhookRequest.builder()
                .id(sepayId)
                .accountNumber(ACCOUNT)
                .transferType("in")
                .content(content)
                .transferAmount(BigDecimal.valueOf(amount))
                .build();
    }

    private Payment pendingSepay(long paymentId, long amount) {
        return Payment.builder()
                .id(paymentId)
                .method(PaymentMethod.SEPAY)
                .status(PaymentStatus.PENDING)
                .amount(BigDecimal.valueOf(amount))
                .build();
    }

    // ---------- Xác thực ----------

    @Test
    void apiKeyDung_chapNhan() {
        assertThat(sepayService.isAuthorized("Apikey " + API_KEY)).isTrue();
    }

    @Test
    void apiKeySai_thieu_hoacSaiKieu_tuChoi() {
        assertThat(sepayService.isAuthorized("Apikey khoa-sai")).isFalse();
        assertThat(sepayService.isAuthorized(null)).isFalse();
        assertThat(sepayService.isAuthorized("Bearer " + API_KEY)).isFalse();
    }

    @Test
    void chuaCauHinhApiKey_tuChoiMoiRequest_thayViMoToang() {
        ReflectionTestUtils.setField(sepayService, "apiKey", "");
        // Nếu chưa cấu hình mà lại chấp nhận, ai cũng đánh dấu được đơn là đã trả tiền.
        assertThat(sepayService.isAuthorized("Apikey ")).isFalse();
    }

    // ---------- Luồng thành công ----------

    @Test
    void chuyenDuTien_ghiNhanThanhToan_vaChuyenDonSangPaid() {
        Payment payment = pendingSepay(7L, 100_000);
        Order order = mock(Order.class);
        when(order.getStatus()).thenReturn(Status.PENDING);
        when(paymentRepository.existsByTransactionId("SEPAY-500")).thenReturn(false);
        when(paymentRepository.findByOrder_Id(19L)).thenReturn(Optional.of(payment));
        when(paymentRepository.markPaidIfPending(eq(7L), eq("SEPAY-500"), any(),
                eq(PaymentStatus.PENDING), eq(PaymentStatus.SUCCESS))).thenReturn(1);
        when(orderRepository.findById(19L)).thenReturn(Optional.of(order));

        var result = sepayService.handleWebhook(webhook(500, "DH19 thanh toan don hang", 100_000));

        assertThat(result).isEqualTo(SepayService.WebhookResult.PROCESSED);
        verify(order).setStatus(Status.PAID);
    }

    // ---------- Các lý do phải bỏ qua ----------

    @Test
    void chuyenThieuTien_giuTrangThaiCho_khongGhiNhan() {
        when(paymentRepository.existsByTransactionId("SEPAY-501")).thenReturn(false);
        when(paymentRepository.findByOrder_Id(19L)).thenReturn(Optional.of(pendingSepay(7L, 100_000)));

        var result = sepayService.handleWebhook(webhook(501, "DH19", 50_000));

        assertThat(result).isEqualTo(SepayService.WebhookResult.IGNORED);
        verify(paymentRepository, never()).markPaidIfPending(anyLong(), any(), any(), any(), any());
    }

    @Test
    void webhookTrung_daXuLyRoi_boQua() {
        // SePay gửi lại tới 7 lần nếu lần trước không nhận được phản hồi.
        when(paymentRepository.existsByTransactionId("SEPAY-502")).thenReturn(true);

        var result = sepayService.handleWebhook(webhook(502, "DH19", 100_000));

        assertThat(result).isEqualTo(SepayService.WebhookResult.IGNORED);
        verify(paymentRepository, never()).findByOrder_Id(anyLong());
    }

    @Test
    void haiWebhookDuaNhau_benThuaKhongCapNhatDonLanHai() {
        when(paymentRepository.existsByTransactionId("SEPAY-503")).thenReturn(false);
        when(paymentRepository.findByOrder_Id(19L)).thenReturn(Optional.of(pendingSepay(7L, 100_000)));
        // Câu UPDATE có điều kiện status = PENDING trả 0: request khác vừa ghi trước.
        when(paymentRepository.markPaidIfPending(anyLong(), any(), any(), any(), any())).thenReturn(0);

        var result = sepayService.handleWebhook(webhook(503, "DH19", 100_000));

        assertThat(result).isEqualTo(SepayService.WebhookResult.IGNORED);
        verify(orderRepository, never()).findById(anyLong());
    }

    @Test
    void tienRa_khongPhaiTienVao_boQua() {
        SepayWebhookRequest request = webhook(504, "DH19", 100_000);
        request.setTransferType("out");

        assertThat(sepayService.handleWebhook(request)).isEqualTo(SepayService.WebhookResult.IGNORED);
        verify(paymentRepository, never()).existsByTransactionId(any());
    }

    @Test
    void tienVeTaiKhoanKhac_boQua() {
        SepayWebhookRequest request = webhook(505, "DH19", 100_000);
        request.setAccountNumber("9999999999");

        assertThat(sepayService.handleWebhook(request)).isEqualTo(SepayService.WebhookResult.IGNORED);
        verify(paymentRepository, never()).existsByTransactionId(any());
    }

    @Test
    void khoanTienKhongKemMaDon_boQua() {
        when(paymentRepository.existsByTransactionId("SEPAY-506")).thenReturn(false);

        var result = sepayService.handleWebhook(webhook(506, "chuyen tien an trua", 100_000));

        assertThat(result).isEqualTo(SepayService.WebhookResult.IGNORED);
        verify(paymentRepository, never()).findByOrder_Id(anyLong());
    }

    // ---------- Tách mã đơn ----------

    @Test
    void tachMaDon_tuTruongCode_hoacTuNoiDungLonXon() {
        SepayWebhookRequest fromCode = SepayWebhookRequest.builder().code("DH42").content("chuyen tien").build();
        SepayWebhookRequest messy = SepayWebhookRequest.builder().content("MBVCB.3278907687.DH19.CT tu 0123").build();
        SepayWebhookRequest lowercase = SepayWebhookRequest.builder().content("dh7 thanh toan").build();

        assertThat(sepayService.extractOrderId(fromCode)).contains(42L);
        assertThat(sepayService.extractOrderId(messy)).contains(19L);
        assertThat(sepayService.extractOrderId(lowercase)).contains(7L);
    }

    // ---------- Thông tin chuyển khoản ----------

    @Test
    void thanhToanSepayDangCho_coThongTinChuyenKhoanVaQr() {
        PaymentResponse response = PaymentResponse.builder()
                .orderId(19L)
                .method(PaymentMethod.SEPAY)
                .status(PaymentStatus.PENDING)
                .amount(new BigDecimal("1035000.00"))
                .build();

        sepayService.withTransferInfo(response);

        assertThat(response.getTransferContent()).isEqualTo("DH19");
        assertThat(response.getQrUrl())
                .contains("acc=" + ACCOUNT)
                .contains("bank=MBBank")
                .contains("amount=1035000")
                .contains("des=DH19");
    }

    @Test
    void thanhToanDaXong_khongHienQr_deKhachKhongChuyenLanHai() {
        PaymentResponse response = PaymentResponse.builder()
                .orderId(19L)
                .method(PaymentMethod.SEPAY)
                .status(PaymentStatus.SUCCESS)
                .amount(new BigDecimal("1035000.00"))
                .build();

        sepayService.withTransferInfo(response);

        assertThat(response.getQrUrl()).isNull();
        assertThat(response.getTransferContent()).isNull();
    }
}
