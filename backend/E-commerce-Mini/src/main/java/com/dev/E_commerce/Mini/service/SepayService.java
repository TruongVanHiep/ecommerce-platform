package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.SepayWebhookRequest;
import com.dev.E_commerce.Mini.dto.response.PaymentResponse;
import com.dev.E_commerce.Mini.entity.Payment;
import com.dev.E_commerce.Mini.enums.PaymentMethod;
import com.dev.E_commerce.Mini.enums.PaymentStatus;
import com.dev.E_commerce.Mini.enums.Status;
import com.dev.E_commerce.Mini.repository.OrderRepository;
import com.dev.E_commerce.Mini.repository.PaymentRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thanh toán chuyển khoản ngân hàng qua SePay.
 *
 * <p>Luồng: khách chuyển khoản với nội dung "DH&lt;mã đơn&gt;" → ngân hàng báo
 * biến động số dư cho SePay → SePay POST webhook về đây → khớp nội dung với
 * đơn, kiểm tra số tiền → chuyển thanh toán sang SUCCESS và đơn sang PAID.
 *
 * <p>Webhook là endpoint public nên mọi thứ SePay gửi đều bị coi là không đáng
 * tin cho tới khi kiểm xong: đúng API key, đúng tài khoản nhận, tiền vào chứ
 * không phải tiền ra, chưa xử lý giao dịch này bao giờ, mã đơn có thật và đang
 * chờ thanh toán SePay, và số tiền đủ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SepayService {

    private static final String QR_BASE_URL = "https://vietqr.app/img";
    private static final String TX_PREFIX = "SEPAY-";

    PaymentRepository paymentRepository;
    OrderRepository orderRepository;
    MeterRegistry meterRegistry;

    // @NonFinal: @FieldDefaults(makeFinal = true) sẽ biến các trường này thành
    // final, và Spring không tiêm @Value vào trường final được — app sẽ chết lúc
    // khởi động với lỗi "required a bean of type String".
    @NonFinal @Value("${app.sepay.api-key:}")
    String apiKey;

    @NonFinal @Value("${app.sepay.bank-code:}")
    String bankCode;

    @NonFinal @Value("${app.sepay.account-number:}")
    String accountNumber;

    @NonFinal @Value("${app.sepay.account-name:}")
    String accountName;

    @NonFinal @Value("${app.sepay.transfer-prefix:DH}")
    String transferPrefix;

    /** Kết quả xử lý webhook. Controller dùng để quyết định phản hồi. */
    public enum WebhookResult {
        /** Giao dịch khớp một đơn đang chờ và đã được ghi nhận thanh toán. */
        PROCESSED,
        /**
         * Hợp lệ về mặt xác thực nhưng không làm gì: giao dịch không liên quan
         * đơn nào, đã xử lý rồi, hoặc chuyển thiếu tiền. Vẫn phải trả
         * {@code success: true}, nếu không SePay sẽ gửi lại tới 7 lần.
         */
        IGNORED
    }

    /** Đủ cấu hình để khách thanh toán được chưa. */
    public boolean isEnabled() {
        return !apiKey.isBlank() && !bankCode.isBlank() && !accountNumber.isBlank();
    }

    public String transferContentFor(Long orderId) {
        return transferPrefix + orderId;
    }

    /**
     * Gắn thông tin chuyển khoản vào response để frontend hiện mã QR. Chỉ khi
     * thanh toán là SePay và còn đang chờ — đã thanh toán rồi thì hiện QR ra chỉ
     * khiến khách chuyển tiền lần hai.
     */
    public PaymentResponse withTransferInfo(PaymentResponse response) {
        if (response == null
                || response.getMethod() != PaymentMethod.SEPAY
                || response.getStatus() != PaymentStatus.PENDING) {
            return response;
        }
        String content = transferContentFor(response.getOrderId());
        response.setBankCode(bankCode);
        response.setAccountNumber(accountNumber);
        response.setAccountName(accountName);
        response.setTransferContent(content);
        response.setQrUrl(buildQrUrl(response.getAmount(), content));
        return response;
    }

    private String buildQrUrl(BigDecimal amount, String content) {
        return UriComponentsBuilder.fromUriString(QR_BASE_URL)
                .queryParam("acc", accountNumber)
                .queryParam("bank", bankCode)
                // Làm tròn LÊN: tổng đơn có phần lẻ thì khách chuyển dư vài đồng
                // vẫn qua bước kiểm tra số tiền, còn làm tròn xuống thì thiếu.
                .queryParam("amount", amount.setScale(0, RoundingMode.UP).toPlainString())
                .queryParam("des", content)
                .queryParam("template", "compact")
                .encode()
                .toUriString();
    }

    /**
     * So khớp header {@code Authorization: Apikey <key>}.
     *
     * <p>Dùng {@link MessageDigest#isEqual} chứ không phải {@code equals}:
     * equals dừng ngay ở ký tự sai đầu tiên, nên thời gian phản hồi lộ ra kẻ
     * dò khoá đã đoán đúng bao nhiêu ký tự đầu.
     */
    public boolean isAuthorized(String authorizationHeader) {
        if (apiKey.isBlank()) {
            // Đóng cửa khi chưa cấu hình, thay vì mở toang: thiếu khoá mà chấp
            // nhận mọi request thì ai cũng đánh dấu được đơn là đã thanh toán.
            log.error("Nhận webhook SePay nhưng SEPAY_API_KEY chưa cấu hình — từ chối");
            return false;
        }
        if (authorizationHeader == null) {
            return false;
        }
        byte[] expected = ("Apikey " + apiKey).getBytes(StandardCharsets.UTF_8);
        byte[] actual = authorizationHeader.trim().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    /**
     * Xử lý một webhook đã qua xác thực.
     *
     * <p>Lỗi bất ngờ (DB sập...) cứ để ném ra: controller trả 500 và SePay gửi
     * lại sau, đúng hành vi mong muốn. Chỉ những trường hợp đã biết là "không
     * cần làm gì" mới trả IGNORED.
     */
    @Transactional
    public WebhookResult handleWebhook(SepayWebhookRequest request) {
        if (request.getId() == null) {
            log.warn("Webhook SePay thiếu id giao dịch — bỏ qua");
            return WebhookResult.IGNORED;
        }
        String transactionId = TX_PREFIX + request.getId();

        if (!"in".equalsIgnoreCase(request.getTransferType())) {
            return WebhookResult.IGNORED;
        }

        // Một tài khoản SePay có thể liên kết nhiều tài khoản ngân hàng; chỉ
        // tiền về đúng tài khoản cấu hình cho shop mới được tính.
        if (!accountNumber.equals(request.getAccountNumber())
                && !accountNumber.equals(request.getSubAccount())) {
            log.info("Webhook SePay {}: tiền về tài khoản khác — bỏ qua", transactionId);
            return WebhookResult.IGNORED;
        }

        if (paymentRepository.existsByTransactionId(transactionId)) {
            log.info("Webhook SePay {}: đã xử lý trước đó — bỏ qua", transactionId);
            return WebhookResult.IGNORED;
        }

        Optional<Long> orderId = extractOrderId(request);
        if (orderId.isEmpty()) {
            // Khoản tiền không kèm mã đơn: lương, bạn bè chuyển... Chuyện bình thường.
            log.info("Webhook SePay {}: nội dung không có mã đơn — bỏ qua", transactionId);
            return WebhookResult.IGNORED;
        }

        Optional<Payment> found = paymentRepository.findByOrder_Id(orderId.get());
        if (found.isEmpty()
                || found.get().getMethod() != PaymentMethod.SEPAY
                || found.get().getStatus() != PaymentStatus.PENDING) {
            log.info("Webhook SePay {}: đơn #{} không có thanh toán SePay đang chờ — bỏ qua",
                    transactionId, orderId.get());
            return WebhookResult.IGNORED;
        }
        Payment payment = found.get();

        BigDecimal received = request.getTransferAmount() == null ? BigDecimal.ZERO : request.getTransferAmount();
        if (received.compareTo(payment.getAmount()) < 0) {
            // Giữ PENDING thay vì đánh dấu thất bại: khách có thể chuyển nốt phần
            // còn thiếu, hoặc admin xử lý tay. Ghi metric để thấy được trên Grafana.
            log.warn("Webhook SePay {}: đơn #{} nhận {} nhưng cần {} — giữ trạng thái chờ",
                    transactionId, orderId.get(), received, payment.getAmount());
            meterRegistry.counter("payments.sepay.underpaid").increment();
            return WebhookResult.IGNORED;
        }

        int updated = paymentRepository.markPaidIfPending(
                payment.getId(), transactionId, LocalDateTime.now(),
                PaymentStatus.PENDING, PaymentStatus.SUCCESS);
        if (updated == 0) {
            // Một webhook trùng khác vừa thắng cuộc đua ghi.
            log.info("Webhook SePay {}: đơn #{} vừa được xử lý bởi request khác — bỏ qua",
                    transactionId, orderId.get());
            return WebhookResult.IGNORED;
        }

        orderRepository.findById(orderId.get()).ifPresent(order -> {
            // Chỉ nâng PENDING lên PAID. Đơn admin đã chuyển sang trạng thái sau
            // (CONFIRMED, SHIPPING...) thì không kéo ngược về.
            if (order.getStatus() == Status.PENDING) {
                order.setStatus(Status.PAID);
            }
        });

        meterRegistry.counter("payments.processed", "method", PaymentMethod.SEPAY.name(),
                "status", PaymentStatus.SUCCESS.name()).increment();
        log.info("Webhook SePay {}: đơn #{} đã thanh toán {}", transactionId, orderId.get(), received);
        return WebhookResult.PROCESSED;
    }

    /**
     * Lấy mã đơn từ giao dịch. Ưu tiên trường {@code code} (SePay tự tách nếu đã
     * cấu hình tiền tố), rồi mới dò trong nội dung. Nội dung ngân hàng ghi rất lộn
     * xộn — "DH19", "MBVCB.123.DH19.CT", "DH19 thanh toan" — nên dò theo mẫu
     * chứ không so khớp nguyên chuỗi.
     */
    Optional<Long> extractOrderId(SepayWebhookRequest request) {
        Pattern pattern = Pattern.compile(Pattern.quote(transferPrefix) + "(\\d+)", Pattern.CASE_INSENSITIVE);
        for (String source : new String[]{request.getCode(), request.getContent(), request.getDescription()}) {
            if (source == null) {
                continue;
            }
            Matcher matcher = pattern.matcher(source);
            if (matcher.find()) {
                try {
                    return Optional.of(Long.parseLong(matcher.group(1)));
                } catch (NumberFormatException e) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }
}
