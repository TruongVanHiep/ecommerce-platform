package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {
    private final NotificationService notificationService;

    /**
     * Gửi email xác nhận khi đơn đã THANH TOÁN XONG, không phải lúc vừa tạo đơn
     * (xem OrderPaidEvent để biết vì sao đổi).
     *
     * AFTER_COMMIT: chỉ chạy khi transaction ghi nhận thanh toán đã commit, nên
     * rollback không bao giờ sinh ra email cho một khoản thanh toán không tồn tại.
     * @Async: đẩy việc gửi (có retry) sang luồng nền, request gốc — hay webhook
     * SePay đang chờ phản hồi trong 30 giây — không phải đợi máy chủ mail.
     */
    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        try {
            notificationService.sendOrderConfirmation(
                    event.orderId(), event.userEmail(), event.userName(), event.totalPrice());
        } catch (Exception e) {
            log.error("Unexpected error notifying order #{}", event.orderId(), e);
        }
    }
}
