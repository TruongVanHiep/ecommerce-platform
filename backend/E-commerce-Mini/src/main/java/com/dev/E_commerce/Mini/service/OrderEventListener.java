package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.event.OrderCreatedEvent;
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
     * AFTER_COMMIT: only fires once the order transaction has actually committed,
     * so a rollback (e.g. duplicate idempotency key) can never trigger a
     * confirmation email for an order that doesn't exist.
     * @Async: hands the retryable send off to a background thread so the
     * original HTTP request never waits on it.
     */
    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        try {
            notificationService.sendOrderConfirmation(
                    event.orderId(), event.userEmail(), event.userName(), event.totalPrice());
        } catch (Exception e) {
            log.error("Unexpected error notifying order #{}", event.orderId(), e);
        }
    }
}
