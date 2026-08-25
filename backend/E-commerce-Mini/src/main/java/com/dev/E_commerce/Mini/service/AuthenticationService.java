package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.AuthenticationRequest;
import com.dev.E_commerce.Mini.dto.request.IntrospectRequest;
import com.dev.E_commerce.Mini.dto.response.AuthenticationResponse;
import com.dev.E_commerce.Mini.dto.response.IntrospectResponse;
import com.dev.E_commerce.Mini.entity.Role;
import com.dev.E_commerce.Mini.entity.User;
import com.dev.E_commerce.Mini.exception.AppException;
import com.dev.E_commerce.Mini.exception.ErrorCode;
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

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    @NonFinal
    @Value("${jwt.signerKey}")
    private String signerKey;

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

    @Transactional(readOnly = true)
    public AuthenticationResponse authenticate(AuthenticationRequest request){
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .isAuthenticated(authenticated)
                .build();
    }

    @Transactional
    public AuthenticationResponse authenticateGoogle(OAuth2User principal) {
        String email = getRequiredEmail(principal);
        String fullName = getDisplayName(principal, email);

        User user = userRepository.findByEmail(email)
                .or(() -> userRepository.findByUsername(email))
                .orElseGet(() -> createGoogleUser(email, fullName));

        return AuthenticationResponse.builder()
                .token(generateToken(user))
                .isAuthenticated(true)
                .build();
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
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli()
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
