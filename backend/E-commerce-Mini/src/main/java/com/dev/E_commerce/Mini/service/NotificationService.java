package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.exception.NotificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final JavaMailSender mailSender;

    // Runs on the caller's thread — OrderEventListener is what makes this async,
    // so this method itself just needs to be resilient (retry) and can stay synchronous.
    @Retryable(
            retryFor = NotificationException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2) // 1s, 2s, 4s
    )
    public void sendOrderConfirmation(Long orderId, String email, String userName, BigDecimal totalPrice) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("MiniCommerce - Xác nhận đơn hàng #" + orderId);
            message.setText(buildBody(orderId, userName, totalPrice));
            mailSender.send(message);
            log.info("Sent confirmation email for order #{} to {}", orderId, email);
        } catch (MailException e) {
            throw new NotificationException("Failed to send confirmation email for order #" + orderId, e);
        }
    }

    @Recover
    public void recoverFromNotificationFailure(
            NotificationException e, Long orderId, String email, String userName, BigDecimal totalPrice) {
        log.error("Failed to send confirmation for order #{} to {} after retries", orderId, email, e);
        // TODO: lưu vào bảng "failed_notifications" để xử lý thủ công sau, nếu cần
    }

    private String buildBody(Long orderId, String userName, BigDecimal totalPrice) {
        String greeting = (userName != null && !userName.isBlank()) ? userName : "Quý khách";
        NumberFormat vndFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        return """
                Xin chào %s,

                Cảm ơn bạn đã đặt hàng tại MiniCommerce!

                Mã đơn hàng: #%d
                Tổng thanh toán: %s VNĐ

                Đơn hàng của bạn đang được xử lý và sẽ sớm được giao đến bạn.

                Trân trọng,
                MiniCommerce
                """.formatted(greeting, orderId, vndFormat.format(totalPrice));
    }
}
