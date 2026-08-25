package com.dev.E_commerce.Mini.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Log 1 dòng cho mỗi HTTP request (method, path, status, thời gian xử lý, user).
 * Không log header Authorization/Cookie hay body request — chỉ mask query string
 * nếu chứa các tham số nhạy cảm (token, password, code, client_secret, ...), vì
 * application-secrets.yaml đang chứa secret thật và không được để lọt vào log.
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Pattern SENSITIVE_PARAM = Pattern.compile(
            "(?i)(token|password|secret|code|client_secret)=([^&]+)"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        Exception error = null;
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            error = ex;
            throw ex;
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            String user = resolveUsername();
            String query = maskSensitiveParams(request.getQueryString());
            String uri = query != null ? request.getRequestURI() + "?" + query : request.getRequestURI();
            // Nếu request văng exception chưa được xử lý, response.getStatus() ở đây vẫn còn
            // là giá trị mặc định (200) — ErrorPageFilter của Spring Boot chỉ set status thật
            // SAU khi exception thoát khỏi filter này. Ép về 500 để log không sai lệch.
            int status = error != null ? HttpServletResponse.SC_INTERNAL_SERVER_ERROR : response.getStatus();

            if (error != null) {
                log.error("HTTP {} {} status={} durationMs={} user={} error={}",
                        request.getMethod(), uri, status, durationMs, user, error.toString());
            } else {
                log.info("HTTP {} {} status={} durationMs={} user={}",
                        request.getMethod(), uri, status, durationMs, user);
            }
        }
    }

    private String resolveUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        return authentication.getName();
    }

    private String maskSensitiveParams(String queryString) {
        if (queryString == null) {
            return null;
        }
        return SENSITIVE_PARAM.matcher(queryString).replaceAll("$1=***");
    }
}
