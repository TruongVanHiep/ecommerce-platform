package com.dev.E_commerce.Mini;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Kiểm tra toàn bộ Spring context nạp được: mọi bean khởi tạo thành công,
 * Hibernate dựng được schema, không có cấu hình nào mâu thuẫn.
 *
 * <p>Trước đây test này kết nối thẳng tới MySQL ở {@code localhost:3306} nên
 * kết quả phụ thuộc máy chạy: máy không cài MySQL thì "connection refused",
 * máy cài MariaDB ở cổng đó thì lỗi plugin xác thực. Giờ Testcontainers tự
 * dựng một MySQL sạch trong Docker, chạy xong tự xoá — kết quả giống nhau ở
 * mọi máy và trên CI.
 *
 * <p>Yêu cầu duy nhất: Docker đang chạy.
 */
@Testcontainers
@SpringBootTest(properties = {
        // Các biến này trong application.yaml không có giá trị mặc định, thiếu
        // là context không nạp được. Đây chỉ là giá trị giả để test chạy.
        "GOOGLE_CLIENT_ID=test-client-id",
        "GOOGLE_CLIENT_SECRET=test-client-secret",
        "MAIL_USERNAME=test@example.com",
        "MAIL_PASSWORD=test-password",
        "DB_PASSWORD=test-password",
        // JWT ký bằng HS512 nên khoá phải đủ 64 byte, ngắn hơn là app không
        // khởi động được.
        "JWT_SIGNER_KEY=0123456789012345678901234567890123456789012345678901234567890123",
        // Không có Tempo khi chạy test; để mặc định 1.0 thì mỗi request lại cố
        // gửi trace tới localhost:4318 và ghi đầy log lỗi.
        "TRACING_SAMPLE_RATE=0"
})
class ECommerceMiniApplicationTests {

    /**
     * {@code @ServiceConnection} tự trỏ datasource của ứng dụng vào container
     * này, không cần khai báo tay url/username/password.
     */
    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Test
    void contextLoads() {
    }
}
