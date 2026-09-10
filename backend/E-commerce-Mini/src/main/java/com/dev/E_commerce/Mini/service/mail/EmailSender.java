package com.dev.E_commerce.Mini.service.mail;

/**
 * Lớp trừu tượng cho việc gửi email, để đổi nhà cung cấp mà không đụng vào
 * logic nghiệp vụ.
 *
 * <p>Vì sao cần: ban đầu gửi thẳng qua SMTP của Gmail bằng app password. Cách
 * đó gửi được nhưng thư luôn rơi vào thư rác — địa chỉ {@code @gmail.com} không
 * có SPF/DKIM chứng minh quyền gửi thay cho một tên miền, nên thư giao dịch từ
 * nguồn này bị bộ lọc nghi ngờ. Sửa nội dung thư chỉ giảm bớt chứ không giải
 * quyết được gốc.
 *
 * <p>Hai cài đặt hiện có:
 * <ul>
 *   <li>{@link SmtpEmailSender} — mặc định, dùng khi chạy local. Không cần
 *       đăng ký dịch vụ ngoài.</li>
 *   <li>{@link ResendEmailSender} — dùng ở production. Gửi qua API HTTPS từ
 *       tên miền riêng đã cấu hình SPF/DKIM.</li>
 * </ul>
 */
public interface EmailSender {

    /**
     * @param html bản HTML để hiển thị
     * @param text bản chữ thuần, dành cho ứng dụng mail không đọc HTML — thiếu
     *             nó cũng làm tăng điểm spam
     * @throws com.dev.E_commerce.Mini.exception.NotificationException khi gửi
     *         thất bại, để tầng trên retry
     */
    void send(String to, String subject, String html, String text);
}
