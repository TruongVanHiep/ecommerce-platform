package com.dev.E_commerce.Mini.service.mail;

import com.dev.E_commerce.Mini.exception.NotificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Gửi email qua API HTTPS của Resend.
 *
 * <p>Dùng ở production vì hai lý do:
 * <ol>
 *   <li>Gửi từ tên miền riêng đã cấu hình SPF/DKIM nên thư vào hộp thư đến
 *       thay vì thư rác — điều mà Gmail cá nhân không làm được.</li>
 *   <li>Đi qua cổng 443 chứ không phải cổng SMTP (25/465/587). Nhiều nền tảng
 *       hosting chặn các cổng SMTP để chống spam, khi đó gửi qua SMTP sẽ treo
 *       tới lúc timeout mà không có thông báo rõ ràng.</li>
 * </ol>
 *
 * <p>Bật bằng cách đặt {@code MAIL_PROVIDER=resend} và {@code RESEND_API_KEY}.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "resend")
public class ResendEmailSender implements EmailSender {

    private static final String API_URL = "https://api.resend.com/emails";

    private final RestClient restClient;
    private final String from;

    public ResendEmailSender(
            @Value("${app.mail.resend-api-key}") String apiKey,
            @Value("${app.mail.from-address}") String fromAddress,
            @Value("${app.mail.from-name:MiniCommerce}") String fromName) {

        if (apiKey == null || apiKey.isBlank()) {
            // Chết ngay lúc khởi động thay vì lúc có đơn hàng đầu tiên: sai cấu
            // hình mà app vẫn chạy thì lỗi chỉ lộ ra khi khách đã đặt hàng.
            throw new IllegalStateException(
                    "MAIL_PROVIDER=resend nhưng thiếu RESEND_API_KEY");
        }
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalStateException(
                    "MAIL_PROVIDER=resend nhưng thiếu MAIL_FROM_ADDRESS");
        }

        this.from = "%s <%s>".formatted(fromName, fromAddress);
        this.restClient = RestClient.builder()
                .baseUrl(API_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    @Override
    public void send(String to, String subject, String html, String text) {
        try {
            restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "from", from,
                            "to", List.of(to),
                            "subject", subject,
                            "html", html,
                            "text", text))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Đã gửi email qua Resend tới {}", to);
        } catch (Exception e) {
            // Bọc lại thành NotificationException để @Retryable ở
            // NotificationService thử lại. KHÔNG ghi nội dung phản hồi của
            // Resend vào log vì nó có thể chứa lại địa chỉ email và tiêu đề.
            throw new NotificationException("Gửi email qua Resend thất bại tới " + to, e);
        }
    }
}
