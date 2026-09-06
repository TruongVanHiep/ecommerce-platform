package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.entity.User;
import com.dev.E_commerce.Mini.repository.RefreshTokenRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bean riêng là CỐ Ý, cùng lý do với OrderLookupService/PaymentLookupService:
 *
 * Khi phát hiện refresh token đã thu hồi bị dùng lại, AuthenticationService phải
 * (1) thu hồi toàn bộ phiên của user rồi (2) ném lỗi để từ chối request. Nhưng
 * ném lỗi trong cùng transaction sẽ ROLLBACK luôn lệnh thu hồi ở bước 1 —
 * biện pháp bảo vệ trở thành vô tác dụng.
 *
 * REQUIRES_NEW mở transaction riêng, commit độc lập nên lệnh thu hồi vẫn có hiệu
 * lực dù transaction gọi nó bị rollback. Lưu ý: REQUIRES_NEW chỉ hoạt động khi
 * gọi qua proxy Spring — gọi this.method() trong cùng class sẽ bị bỏ qua.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenSecurityService {
    RefreshTokenRepository refreshTokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllSessions(User user) {
        int revoked = refreshTokenRepository.revokeAllByUser(user);
        log.warn("Đã thu hồi {} phiên đăng nhập của username={} do nghi refresh token bị đánh cắp",
                revoked, user.getUsername());
    }
}
