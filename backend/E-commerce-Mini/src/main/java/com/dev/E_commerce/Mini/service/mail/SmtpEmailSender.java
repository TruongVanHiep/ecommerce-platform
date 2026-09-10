package com.dev.E_commerce.Mini.service.mail;

import com.dev.E_commerce.Mini.exception.NotificationException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Gửi qua SMTP (Gmail). Mặc định khi không cấu hình gì — đủ dùng cho môi
 * trường phát triển.
 *
 * <p>KHÔNG nên dùng ở production: thư gửi từ địa chỉ Gmail cá nhân gần như
 * chắc chắn vào thư rác của người nhận. Dùng {@link ResendEmailSender} thay thế.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "smtp", matchIfMissing = true)
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from-address:}")
    private String fromAddress;

    @Value("${app.mail.from-name:MiniCommerce}")
    private String fromName;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Override
    public void send(String to, String subject, String html, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true ở tham số thứ 2 = multipart, cần thiết để gửi kèm cả bản
            // chữ thuần lẫn bản HTML trong cùng một thư.
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            // Gmail chỉ cho gửi từ chính tài khoản đã xác thực; đặt địa chỉ khác
            // sẽ bị nó ghi đè hoặc từ chối. Nhưng TÊN hiển thị thì đặt được, và
            // "MiniCommerce" đọc dễ chịu hơn một địa chỉ Gmail trơ trọi.
            String from = fromAddress.isBlank() ? mailUsername : fromAddress;
            helper.setFrom(from, fromName);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, html);

            mailSender.send(message);
            log.info("Đã gửi email qua SMTP tới {}", to);
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            throw new NotificationException("Gửi email qua SMTP thất bại tới " + to, e);
        }
    }
}
