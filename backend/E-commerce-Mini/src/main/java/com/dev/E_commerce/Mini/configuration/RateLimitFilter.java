package com.dev.E_commerce.Mini.configuration;

import com.dev.E_commerce.Mini.dto.response.ApiResponse;
import com.dev.E_commerce.Mini.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Giới hạn số request theo địa chỉ IP, chia làm 3 mức:
 *
 * <ul>
 *   <li>STRICT — các route xác thực (đăng nhập, đăng ký, áp voucher): mặc định
 *       5 lần / 15 phút. Đây là lớp chặn brute force mật khẩu và dò mã voucher.</li>
 *   <li>WRITE  — mọi request ghi dữ liệu (POST/PUT/DELETE) còn lại: 60 / phút.</li>
 *   <li>GLOBAL — toàn bộ request còn lại: 200 / phút.</li>
 * </ul>
 *
 * Bucket lưu trong bộ nhớ tiến trình (Caffeine, tự dọn sau 30 phút không dùng).
 * Nếu sau này chạy nhiều instance backend thì phải chuyển sang store dùng chung
 * (Redis), vì mỗi instance hiện đếm riêng.
 *
 * Khoá bucket dùng {@code request.getRemoteAddr()} chứ KHÔNG tự đọc header
 * X-Forwarded-For: chạy local thì không có proxy tin cậy nào phía trước, đọc
 * header đó thì kẻ tấn công chỉ cần đổi header là thoát giới hạn.
 *
 * Khi deploy sau Caddy, docker-compose.prod.yml bật
 * {@code SERVER_FORWARD_HEADERS_STRATEGY=framework} để Spring tự xử lý các
 * header X-Forwarded-* — lúc đó getRemoteAddr() trả về IP thật của client thay
 * vì IP của Caddy. An toàn vì ở production backend không mở port ra ngoài,
 * chỉ Caddy gọi tới được nên header không giả mạo được.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    /** Các đường dẫn hạ tầng phải luôn đi lọt: Docker healthcheck 10s/lần và Prometheus scrape. */
    private static final List<String> EXCLUDED_PREFIXES = List.of("/actuator");

    /** Các route xác thực/nhạy cảm cần siết chặt nhất. */
    private static final Set<String> STRICT_PATHS = Set.of(
            "/api/auth/login",
            "/api/users",
            "/api/vouchers/apply"
    );

    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.rate-limit.strict-capacity:5}")
    private int strictCapacity;

    @Value("${app.rate-limit.strict-window-minutes:15}")
    private int strictWindowMinutes;

    @Value("${app.rate-limit.write-capacity:60}")
    private int writeCapacity;

    @Value("${app.rate-limit.global-capacity:200}")
    private int globalCapacity;

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .maximumSize(100_000)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Tier tier = resolveTier(request);
        if (tier == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = tier.name() + ":" + request.getRemoteAddr();
        Bucket bucket = buckets.get(key, k -> newBucket(tier));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000);
        meterRegistry.counter("ratelimit.rejected", "tier", tier.name().toLowerCase()).increment();
        log.warn("Rate limit vượt ngưỡng tier={} ip={} path={} retryAfterSeconds={}",
                tier, request.getRemoteAddr(), request.getRequestURI(), retryAfterSeconds);

        writeTooManyRequests(response, retryAfterSeconds);
    }

    private Tier resolveTier(HttpServletRequest request) {
        String path = request.getRequestURI();

        for (String excluded : EXCLUDED_PREFIXES) {
            if (path.startsWith(excluded)) {
                return null;
            }
        }

        String method = request.getMethod();
        // Chỉ siết STRICT với thao tác ghi: GET /api/users là API quản trị đã có
        // @PreAuthorize riêng, không cần chung giới hạn với luồng đăng nhập.
        if (HttpMethod.POST.matches(method) && STRICT_PATHS.contains(path)) {
            return Tier.STRICT;
        }
        if (HttpMethod.POST.matches(method) || HttpMethod.PUT.matches(method) || HttpMethod.DELETE.matches(method)) {
            return Tier.WRITE;
        }
        return Tier.GLOBAL;
    }

    private Bucket newBucket(Tier tier) {
        Bandwidth limit = switch (tier) {
            case STRICT -> Bandwidth.builder()
                    .capacity(strictCapacity)
                    .refillIntervally(strictCapacity, Duration.ofMinutes(strictWindowMinutes))
                    .build();
            case WRITE -> Bandwidth.builder()
                    .capacity(writeCapacity)
                    .refillGreedy(writeCapacity, Duration.ofMinutes(1))
                    .build();
            case GLOBAL -> Bandwidth.builder()
                    .capacity(globalCapacity)
                    .refillGreedy(globalCapacity, Duration.ofMinutes(1))
                    .build();
        };
        return Bucket.builder().addLimit(limit).build();
    }

    private void writeTooManyRequests(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        ErrorCode errorCode = ErrorCode.TOO_MANY_REQUESTS;
        response.setStatus(errorCode.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setHeader("X-RateLimit-Remaining", "0");

        ApiResponse<?> body = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        response.getWriter().write(objectMapper.writeValueAsString(body));
        response.flushBuffer();
    }

    private enum Tier {
        STRICT, WRITE, GLOBAL
    }
}
