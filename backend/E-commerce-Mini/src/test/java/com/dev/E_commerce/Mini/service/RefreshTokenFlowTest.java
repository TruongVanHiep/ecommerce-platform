package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.RefreshTokenRequest;
import com.dev.E_commerce.Mini.entity.RefreshToken;
import com.dev.E_commerce.Mini.entity.Role;
import com.dev.E_commerce.Mini.entity.User;
import com.dev.E_commerce.Mini.exception.AppException;
import com.dev.E_commerce.Mini.exception.ErrorCode;
import com.dev.E_commerce.Mini.repository.RefreshTokenRepository;
import com.dev.E_commerce.Mini.repository.RoleRepository;
import com.dev.E_commerce.Mini.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm tra 3 hành vi bảo mật cốt lõi của refresh token:
 * xoay vòng, phát hiện dùng lại, và thu hồi khi đăng xuất.
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenFlowTest {

    @Mock PasswordEncoder passwordEncoder;
    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;

    AuthenticationService authenticationService;

    User user;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                passwordEncoder, userRepository, roleRepository, refreshTokenRepository);
        ReflectionTestUtils.setField(authenticationService, "signerKey",
                "1234567890123456789012345678901234567890123456789012345678901234");
        ReflectionTestUtils.setField(authenticationService, "accessTokenMinutes", 15L);
        ReflectionTestUtils.setField(authenticationService, "refreshTokenDays", 7L);

        user = User.builder()
                .username("nguoidung")
                .email("nguoidung@example.com")
                .password("hashed")
                .roles(Set.of(Role.builder().name("USER").build()))
                .build();

        lenient().when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private RefreshToken validToken(String value) {
        return RefreshToken.builder()
                .token(value)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
    }

    @Test
    void refresh_capTokenMoi_vaThuHoiTokenCu() {
        RefreshToken current = validToken("token-cu");
        when(refreshTokenRepository.findByToken("token-cu")).thenReturn(Optional.of(current));

        var response = authenticationService.refresh(
                RefreshTokenRequest.builder().refreshToken("token-cu").build());

        // Cấp cặp token mới
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getRefreshToken())
                .isNotBlank()
                .isNotEqualTo("token-cu");
        // Token cũ bị thu hồi ngay (xoay vòng) — dùng lại lần nữa sẽ bị chặn
        assertThat(current.isRevoked()).isTrue();
    }

    @Test
    void refresh_tokenKhongTonTai_thiTuChoi() {
        when(refreshTokenRepository.findByToken("token-bia")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.refresh(
                RefreshTokenRequest.builder().refreshToken("token-bia").build()))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.REFRESH_TOKEN_INVALID);
    }

    @Test
    void refresh_tokenHetHan_thiTuChoi() {
        RefreshToken expired = RefreshToken.builder()
                .token("token-het-han")
                .user(user)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("token-het-han")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authenticationService.refresh(
                RefreshTokenRequest.builder().refreshToken("token-het-han").build()))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    @Test
    void refresh_dungLaiTokenDaThuHoi_thiHuyToanBoPhien() {
        RefreshToken revoked = RefreshToken.builder()
                .token("token-da-thu-hoi")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(true)
                .build();
        when(refreshTokenRepository.findByToken("token-da-thu-hoi")).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> authenticationService.refresh(
                RefreshTokenRequest.builder().refreshToken("token-da-thu-hoi").build()))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.REFRESH_TOKEN_REVOKED);

        // Dấu hiệu token bị đánh cắp => phải huỷ mọi phiên của user
        verify(refreshTokenRepository).revokeAllByUser(user);
    }

    @Test
    void logout_thuHoiTokenTrongDb() {
        RefreshToken current = validToken("token-dang-dung");
        when(refreshTokenRepository.findByToken("token-dang-dung")).thenReturn(Optional.of(current));

        authenticationService.logout(
                RefreshTokenRequest.builder().refreshToken("token-dang-dung").build());

        assertThat(current.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(current);
    }

    @Test
    void logout_tokenKhongTonTai_thiKhongNemLoi() {
        when(refreshTokenRepository.findByToken("token-la")).thenReturn(Optional.empty());

        // Không ném lỗi: nếu báo lỗi thì endpoint logout trở thành công cụ
        // dò xem chuỗi token nào có thật trong hệ thống.
        authenticationService.logout(
                RefreshTokenRequest.builder().refreshToken("token-la").build());

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }
}
