package com.dev.E_commerce.Mini.controller;

import jakarta.validation.Valid;
import com.dev.E_commerce.Mini.dto.request.AuthenticationRequest;
import com.dev.E_commerce.Mini.dto.request.IntrospectRequest;
import com.dev.E_commerce.Mini.dto.request.RefreshTokenRequest;
import com.dev.E_commerce.Mini.dto.response.ApiResponse;
import com.dev.E_commerce.Mini.dto.response.AuthenticationResponse;
import com.dev.E_commerce.Mini.dto.response.IntrospectResponse;
import com.dev.E_commerce.Mini.exception.AppException;
import com.dev.E_commerce.Mini.exception.ErrorCode;
import com.dev.E_commerce.Mini.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest request){
        var authentication = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authentication)
                .build();
    }
    
    /**
     * Đổi refresh token lấy cặp token mới. Endpoint này phải PUBLIC vì lúc client
     * gọi tới thì access token đã hết hạn — chính refresh token là thứ xác thực.
     */
    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody @Valid RefreshTokenRequest request){
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.refresh(request))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody @Valid RefreshTokenRequest request){
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/introspectToken")
    public ApiResponse<IntrospectResponse> introspectToken(@RequestBody @Valid IntrospectRequest request){
        return ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspectToken(request))
                .build();
    }
    @GetMapping("/user-info")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return principal.getAttributes();
    }
}
