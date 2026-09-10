package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.exception.NotificationException;
import com.dev.E_commerce.Mini.service.mail.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // Không phụ thuộc trực tiếp vào JavaMailSender nữa: cách gửi (SMTP hay
    // Resend) do cấu hình quyết định, service này không cần biết.
    private final EmailSender emailSender;

    // Runs on the caller's thread — OrderEventListener is what makes this async,
    // so this method itself just needs to be resilient (retry) and can stay synchronous.
    @Retryable(
            retryFor = NotificationException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2) // 1s, 2s, 4s
    )
    public void sendOrderConfirmation(Long orderId, String email, String userName, BigDecimal totalPrice) {
        String subject = "MiniCommerce - Xác nhận đơn hàng #" + orderId;
        emailSender.send(
                email,
                subject,
                buildHtmlBody(orderId, userName, totalPrice),
                buildTextBody(orderId, userName, totalPrice));
        log.info("Sent confirmation email for order #{} to {}", orderId, email);
    }

    @Recover
    public void recoverFromNotificationFailure(
            NotificationException e, Long orderId, String email, String userName, BigDecimal totalPrice) {
        log.error("Failed to send confirmation for order #{} to {} after retries", orderId, email, e);
        // TODO: lưu vào bảng "failed_notifications" để xử lý thủ công sau, nếu cần
    }

    private String greeting(String userName) {
        return (userName != null && !userName.isBlank()) ? userName : "Quý khách";
    }

    private String formatVnd(BigDecimal totalPrice) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(totalPrice);
    }

    /**
     * Bản chữ thuần. Vẫn phải gửi kèm dù đã có HTML: thư chỉ có HTML mà không
     * có phần text bị bộ lọc spam cộng điểm, và một số ứng dụng mail không
     * hiển thị HTML.
     */
    private String buildTextBody(Long orderId, String userName, BigDecimal totalPrice) {
        return """
                Xin chào %s,

                Cảm ơn bạn đã đặt hàng tại MiniCommerce!

                Mã đơn hàng: #%d
                Tổng thanh toán: %s VNĐ

                Đơn hàng của bạn đang được xử lý và sẽ sớm được giao đến bạn.

                Trân trọng,
                MiniCommerce
                """.formatted(greeting(userName), orderId, formatVnd(totalPrice));
    }

    /**
     * Bản HTML.
     *
     * <p>Dùng CSS nội tuyến và bảng để dàn trang, không dùng flexbox/grid hay
     * thẻ style: nhiều ứng dụng mail (Outlook, Gmail bản web) cắt bỏ thẻ
     * {@code <style>} và không hỗ trợ layout hiện đại.
     */
    private String buildHtmlBody(Long orderId, String userName, BigDecimal totalPrice) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <body style="margin:0;padding:24px;background:#f4f4f5;font-family:Arial,Helvetica,sans-serif;color:#18181b;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:520px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;">
                    <tr>
                      <td style="background:#4f46e5;padding:20px 28px;color:#ffffff;font-size:18px;font-weight:bold;">
                        MiniCommerce
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:28px;">
                        <p style="margin:0 0 16px;font-size:16px;">Xin chào <strong>%s</strong>,</p>
                        <p style="margin:0 0 20px;font-size:14px;line-height:1.6;color:#3f3f46;">
                          Cảm ơn bạn đã đặt hàng tại MiniCommerce. Đơn hàng của bạn đang được xử lý
                          và sẽ sớm được giao đến bạn.
                        </p>
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#fafafa;border-radius:8px;">
                          <tr>
                            <td style="padding:14px 16px;font-size:14px;color:#52525b;">Mã đơn hàng</td>
                            <td style="padding:14px 16px;font-size:14px;font-weight:bold;text-align:right;">#%d</td>
                          </tr>
                          <tr>
                            <td style="padding:14px 16px;font-size:14px;color:#52525b;border-top:1px solid #e4e4e7;">Tổng thanh toán</td>
                            <td style="padding:14px 16px;font-size:16px;font-weight:bold;text-align:right;color:#4f46e5;border-top:1px solid #e4e4e7;">%s VNĐ</td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:0 28px 28px;font-size:12px;color:#a1a1aa;line-height:1.6;">
                        Đây là email tự động xác nhận đơn hàng bạn vừa đặt.<br>
                        Trân trọng, MiniCommerce.
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(greeting(userName), orderId, formatVnd(totalPrice));
    }
}
