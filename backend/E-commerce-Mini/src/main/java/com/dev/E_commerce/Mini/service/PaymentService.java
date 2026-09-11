package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.CreatePaymentRequest;
import com.dev.E_commerce.Mini.dto.response.PaymentResponse;
import com.dev.E_commerce.Mini.entity.Order;
import com.dev.E_commerce.Mini.entity.Payment;
import com.dev.E_commerce.Mini.entity.User;
import com.dev.E_commerce.Mini.enums.PaymentMethod;
import com.dev.E_commerce.Mini.enums.PaymentStatus;
import com.dev.E_commerce.Mini.event.OrderPaidEvent;
import com.dev.E_commerce.Mini.exception.AppException;
import com.dev.E_commerce.Mini.exception.ErrorCode;
import com.dev.E_commerce.Mini.mapper.PaymentMapper;
import com.dev.E_commerce.Mini.repository.OrderRepository;
import com.dev.E_commerce.Mini.repository.PaymentRepository;
import com.dev.E_commerce.Mini.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
    PaymentRepository paymentRepository;
    OrderRepository orderRepository;
    UserRepository userRepository;
    PaymentMapper paymentMapper;
    PaymentLookupService paymentLookupService;
    SepayService sepayService;
    ApplicationEventPublisher eventPublisher;
    MeterRegistry meterRegistry;

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    /**
     * Idempotent: calling this more than once for the same order (e.g. the client
     * retries after a timeout) returns the existing payment instead of creating a duplicate.
     *
     * The findByOrder_Id-then-insert below is still a check-then-act: 2 requests for the
     * same order arriving close enough together can both pass the check before either
     * commits. payments.order_id has a UNIQUE constraint, so the loser fails with
     * DataIntegrityViolationException instead of silently creating a duplicate — caught
     * below and turned into a lookup of the winner's row (same pattern as OrderLookupService).
     */
    @Transactional
    public PaymentResponse createPaymentForOrder(Long orderId, CreatePaymentRequest request) {
        User user = currentUser();
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        Optional<Payment> existing = paymentRepository.findByOrder_Id(orderId);
        if (existing.isPresent()) {
            return sepayService.withTransferInfo(paymentMapper.toPaymentResponse(existing.get()));
        }

        // Chặn trước khi tạo bản ghi: SePay chưa cấu hình mà vẫn cho tạo thì
        // khách nhận một mã QR trỏ tới tài khoản rỗng, chuyển tiền đi đâu không rõ.
        if (request.getMethod() == PaymentMethod.SEPAY && !sepayService.isEnabled()) {
            throw new AppException(ErrorCode.PAYMENT_METHOD_UNAVAILABLE);
        }

        try {
            return sepayService.withTransferInfo(
                    paymentMapper.toPaymentResponse(createNewPayment(order, request.getMethod())));
        } catch (DataIntegrityViolationException e) {
            // Request thua cuộc đua: bên thắng đã tạo thanh toán và tự phát email,
            // nhánh này chỉ trả lại kết quả, không phát thêm lần nữa.
            return sepayService.withTransferInfo(paymentLookupService.findByOrderIdOrThrow(orderId, e));
        }
    }

    private Payment createNewPayment(Order order, PaymentMethod method) {
        boolean isCod = method == PaymentMethod.COD;
        Payment payment = Payment.builder()
                .order(order)
                .method(method)
                .status(isCod ? PaymentStatus.SUCCESS : PaymentStatus.PENDING)
                .amount(order.getTotalPrice())
                .paidAt(isCod ? LocalDateTime.now() : null)
                .build();
        Payment saved = paymentRepository.save(payment);

        meterRegistry.counter("payments.processed", "method", method.name(), "status", saved.getStatus().name()).increment();
        log.info("Payment created paymentId={} orderId={} method={} status={} amount={}",
                saved.getId(), order.getId(), method, saved.getStatus(), saved.getAmount());

        // COD được chốt ngay lúc tạo nên gửi email xác nhận luôn. Phương thức khác
        // còn PENDING thì chờ tới khi tiền về thật (SePay: SepayService#handleWebhook).
        if (saved.getStatus() == PaymentStatus.SUCCESS) {
            eventPublisher.publishEvent(OrderPaidEvent.of(order));
        }

        return saved;
    }

    /**
     * Frontend gọi lặp lại endpoint này để biết webhook SePay đã xác nhận tiền về
     * chưa. Còn PENDING thì trả kèm thông tin chuyển khoản — người dùng tải lại
     * trang vẫn thấy lại được mã QR.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrder(Long orderId) {
        User user = currentUser();
        orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        Payment payment = paymentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTED));
        return sepayService.withTransferInfo(paymentMapper.toPaymentResponse(payment));
    }
}
