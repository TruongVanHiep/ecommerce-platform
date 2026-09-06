package com.dev.E_commerce.Mini.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Refresh token có trạng thái lưu trong DB — khác với access token (JWT) vốn
 * stateless. Nhờ vậy mới thu hồi được phiên đăng nhập: đăng xuất, đổi mật khẩu,
 * hay phát hiện token bị đánh cắp đều có thể vô hiệu hoá ngay.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_user", columnList = "user_id")
})
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true, length = 255)
    String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "expires_at", nullable = false)
    LocalDateTime expiresAt;

    /**
     * Đánh dấu thu hồi thay vì xoá bản ghi: nếu xoá hẳn thì khi kẻ trộm dùng lại
     * token cũ ta chỉ thấy "không tồn tại", không phân biệt được với token bịa.
     * Giữ lại giúp phát hiện hành vi dùng lại (token reuse) — dấu hiệu bị lộ.
     */
    @Column(nullable = false)
    boolean revoked;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    LocalDateTime createdAt;
}
