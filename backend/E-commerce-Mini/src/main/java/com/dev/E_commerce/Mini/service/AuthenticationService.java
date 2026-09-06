package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.AuthenticationRequest;
import com.dev.E_commerce.Mini.dto.request.IntrospectRequest;
import com.dev.E_commerce.Mini.dto.request.RefreshTokenRequest;
import com.dev.E_commerce.Mini.dto.response.AuthenticationResponse;
import com.dev.E_commerce.Mini.dto.response.IntrospectResponse;
import com.dev.E_commerce.Mini.entity.RefreshToken;
import com.dev.E_commerce.Mini.entity.Role;
import com.dev.E_commerce.Mini.entity.User;
import com.dev.E_commerce.Mini.exception.AppException;
import com.dev.E_commerce.Mini.exception.ErrorCode;
import com.dev.E_commerce.Mini.repository.RefreshTokenRepository;
import com.dev.E_commerce.Mini.repository.RoleRepository;
import com.dev.E_commerce.Mini.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.Set;
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RoleRepository roleRepository;
    RefreshTokenRepository refreshTokenRepository;
    RefreshTokenSecurityService refreshTokenSecurityService;

    SecureRandom secureRandom = new SecureRandom();

    @NonFinal
    @Value("${jwt.signerKey}")
    private String signerKey;

    // @NonFinal là BẮT BUỘC: class này dùng @FieldDefaults(makeFinal = true),
    // để final thì Lombok đưa field vào constructor và Spring sẽ đi tìm một bean
    // kiểu long để inject → ứng dụng không khởi động được.
    @NonFinal
    @Value("${jwt.access-token-minutes:15}")
    private long accessTokenMinutes;

    @NonFinal
    @Value("${jwt.refresh-token-days:7}")
    private long refreshTokenDays;

    public IntrospectResponse introspectToken(IntrospectRequest request){
        boolean isValid = true;
        try {
            verifyToken(request.getToken());
        }catch (Exception e){
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    private void verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request){
        // Sai username và sai mật khẩu đều trả về CÙNG một lỗi UNAUTHENTICATED.
        // Trước đây username không tồn tại trả 404 còn sai mật khẩu trả 401,
        // giúp kẻ tấn công dò ra danh sách username hợp lệ (user enumeration).
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return buildTokenPair(user);
    }

    /**
     * Đổi refresh token lấy cặp token mới.
     *
     * Áp dụng XOAY VÒNG (rotation): mỗi refresh token chỉ dùng được đúng 1 lần,
     * dùng xong bị thu hồi và cấp cái mới. Nhờ vậy nếu token bị đánh cắp thì chỉ
     * dùng được tới khi chủ thật sự refresh lần kế tiếp.
     */
    @Transactional
    public AuthenticationResponse refresh(RefreshTokenRequest request){
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));

        // Token đã thu hồi mà vẫn có người mang tới => nhiều khả năng đã bị đánh
        // cắp và dùng lại. Huỷ toàn bộ phiên của user để chặn kẻ tấn công.
        //
        // Việc thu hồi PHẢI chạy trong transaction riêng (REQUIRES_NEW): exception
        // ném ra ngay sau đây sẽ rollback transaction hiện tại, nếu thu hồi nằm
        // cùng transaction thì nó bị rollback theo và biện pháp bảo vệ vô tác dụng.
        if (stored.isRevoked()) {
            refreshTokenSecurityService.revokeAllSessions(stored.getUser());
            throw new AppException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildTokenPair(stored.getUser());
    }

    /**
     * Đăng xuất: thu hồi refresh token trong DB. Access token đang cầm vẫn còn
     * hiệu lực tới khi hết hạn (bản chất của JWT stateless), nhưng vì hạn chỉ
     * còn vài phút nên rủi ro ở mức chấp nhận được.
     */
    @Transactional
    public void logout(RefreshTokenRequest request){
        // Cố tình KHÔNG báo lỗi khi token không tồn tại — nếu báo, endpoint này
        // trở thành công cụ dò xem chuỗi token nào có thật trong hệ thống.
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }

    private AuthenticationResponse buildTokenPair(User user){
        return AuthenticationResponse.builder()
                .token(generateToken(user))
                .refreshToken(createRefreshToken(user).getToken())
                .isAuthenticated(true)
                .build();
    }

    private RefreshToken createRefreshToken(User user){
        // 256 bit ngẫu nhiên từ SecureRandom. KHÔNG dùng UUID.randomUUID() hay
        // java.util.Random làm giá trị bảo mật vì chúng đoán được.
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String tokenValue = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        return refreshTokenRepository.save(RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenDays))
                .revoked(false)
                .build());
    }

    @Transactional
    public AuthenticationResponse authenticateGoogle(OAuth2User principal) {
        String email = getRequiredEmail(principal);
        String fullName = getDisplayName(principal, email);

        User user = userRepository.findByEmail(email)
                .or(() -> userRepository.findByUsername(email))
                .orElseGet(() -> createGoogleUser(email, fullName));

        return buildTokenPair(user);
    }

    private User createGoogleUser(String email, String fullName) {
        Role userRole = roleRepository.findById("USER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        User user = User.builder()
                .username(email)
                .email(email)
                .fullName(fullName)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .roles(Set.of(userRole))
                .build();

        return userRepository.save(user);
    }

    private String getRequiredEmail(OAuth2User principal) {
        String value = principal.getAttribute("email");
        if (value == null || value.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return value;
    }

    private String getDisplayName(OAuth2User principal, String defaultValue) {
        String value = principal.getAttribute("name");
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("hiepdev.com")
                .issueTime(new Date())
                // Hạn ngắn (mặc định 15 phút): access token không thu hồi được nên
                // phải sống ngắn; client tự gia hạn bằng refresh token.
                .expirationTime(new Date(
                        Instant.now().plus(accessTokenMinutes, ChronoUnit.MINUTES).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject =new JWSObject(jwsHeader, payload);
        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        }catch (Exception e){
            log.error("Cannot create token");
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(role ->{
                stringJoiner.add(role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions())){
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
                }
            });
        }
        return stringJoiner.toString();
    }
}
