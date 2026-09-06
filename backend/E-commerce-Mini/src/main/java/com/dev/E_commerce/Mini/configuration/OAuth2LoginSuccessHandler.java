package com.dev.E_commerce.Mini.configuration;

import com.dev.E_commerce.Mini.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {
    final AuthenticationService authenticationService;

    @Value("${app.oauth2.redirect-uri}")
    String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        var authenticationResponse = authenticationService.authenticateGoogle(principal);
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                // Truyền cả refresh token để luồng đăng nhập Google cũng gia hạn được
                // như đăng nhập thường. Dùng fragment (#) thay vì query (?) vì phần
                // fragment không được trình duyệt gửi lên server, không lọt vào access log.
                .fragment("token=" + authenticationResponse.getToken()
                        + "&refreshToken=" + authenticationResponse.getRefreshToken())
                .build(true)
                .toUriString();
        response.sendRedirect(targetUrl);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException exception) throws IOException {
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .fragment("error=google_login_failed")
                .build(true)
                .toUriString();
        response.sendRedirect(targetUrl);
    }
}



