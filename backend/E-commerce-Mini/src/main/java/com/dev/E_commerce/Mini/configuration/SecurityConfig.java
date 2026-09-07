package com.dev.E_commerce.Mini.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import lombok.RequiredArgsConstructor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.List;

@Configuration
@EnableWebSecurity
// Bật kiểm tra @PreAuthorize ở tầng method. Trước đây annotation này bị đặt
// nhầm trên AdminOrderController (một @RestController) — vẫn chạy nhưng sai chỗ
// và dễ bị xoá nhầm khi refactor controller.
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${jwt.signerKey}")
    private String signerKey;

    /** Danh sách origin được phép gọi API, cấu hình qua APP_CORS_ORIGINS. */
    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    // /auth/refresh và /auth/logout phải public: khi client gọi tới thì access token
    // đã hết hạn, chính refresh token mới là thứ dùng để xác thực.
    private final String[] ENDPOINT_PUBLIC = {"/api/users", "/api/auth/login", "/api/auth/refresh", "/api/auth/logout"};
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        httpSecurity.authorizeHttpRequests(request -> request
                // Các public endpoint cho POST/GET
                .requestMatchers(HttpMethod.POST, ENDPOINT_PUBLIC).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                // Xem review là public (giống mọi sàn TMĐT) — chỉ viết/sửa/xoá mới cần đăng nhập.
                .requestMatchers(HttpMethod.GET, "/api/reviews/product/**").permitAll()

                // Actuator: health/info/prometheus/metrics — cần public để Docker healthcheck
                // và Prometheus scrape được (chỉ những endpoint này được expose, xem application.yaml)
                .requestMatchers("/actuator/**").permitAll()

                // Các route public cho OAuth2 / Login
                .requestMatchers("/", "/login", "/login.html", "/login/**", "/error", "/oauth2/**").permitAll()

                // MỌI request còn lại bắt buộc phải authenticate (ĐẶT Ở CUỐI CÙNG)
                .anyRequest().authenticated()
        );

        // Cấu hình OAuth2 Resource Server (JWT)
        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder()))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );

        // Cấu hình OAuth2 Login (Google)
        httpSecurity.oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginSuccessHandler)
        );

        return httpSecurity.build();
    }

    private JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
        return NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Đọc từ biến môi trường APP_CORS_ORIGINS (nhiều origin cách nhau bằng dấu phẩy).
        // KHÔNG dùng "*" ở đây: đang bật allowCredentials(true), mà chuẩn CORS cấm
        // kết hợp "*" với credentials — trình duyệt sẽ chặn toàn bộ request.
        configuration.setAllowedOrigins(allowedOrigins);

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        // Không có dòng này, trình duyệt sẽ KHÔNG đọc được header rate limit
        // (JS chỉ thấy được các header nằm trong danh sách expose).
        configuration.setExposedHeaders(List.of("Retry-After", "X-RateLimit-Remaining"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}


