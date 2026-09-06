package com.dev.E_commerce.Mini.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    /** Access token (JWT) — hạn ngắn, stateless, không tra DB khi xác thực. */
    String token;

    /** Refresh token — hạn dài, lưu DB, dùng để xin access token mới. */
    String refreshToken;

    boolean isAuthenticated;
}
