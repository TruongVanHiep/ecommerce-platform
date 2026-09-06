package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.RefreshToken;
import com.dev.E_commerce.Mini.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /**
     * Thu hồi toàn bộ token còn hiệu lực của 1 user. Dùng khi phát hiện token đã
     * thu hồi lại được mang tới lần nữa — nghi ngờ bị đánh cắp nên huỷ mọi phiên.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user AND rt.revoked = false")
    int revokeAllByUser(@Param("user") User user);

    /** Dọn token hết hạn để bảng không phình theo thời gian. */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);
}
