package com.dev.E_commerce.Mini.configuration;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter(new SimpleMeterRegistry());
        // @Value không được xử lý khi khởi tạo thủ công nên gán tay cho giống cấu hình thật.
        ReflectionTestUtils.setField(filter, "strictCapacity", 5);
        ReflectionTestUtils.setField(filter, "strictWindowMinutes", 15);
        ReflectionTestUtils.setField(filter, "writeCapacity", 60);
        ReflectionTestUtils.setField(filter, "globalCapacity", 200);
    }

    private MockHttpServletResponse callLogin(String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, mock(FilterChain.class));
        return response;
    }

    @Test
    void login_choPhepDung5LanRoiChan_lanThu6() throws Exception {
        for (int i = 1; i <= 5; i++) {
            assertThat(callLogin("10.0.0.1").getStatus())
                    .as("lần thử thứ %d phải được cho qua", i)
                    .isEqualTo(200);
        }

        MockHttpServletResponse blocked = callLogin("10.0.0.1");
        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader("Retry-After")).isNotNull();
        assertThat(blocked.getHeader("X-RateLimit-Remaining")).isEqualTo("0");
        assertThat(blocked.getContentAsString()).contains("1070");
    }

    @Test
    void moiIpDemRieng() throws Exception {
        for (int i = 1; i <= 5; i++) {
            callLogin("10.0.0.2");
        }
        assertThat(callLogin("10.0.0.2").getStatus()).isEqualTo(429);
        // IP khác vẫn phải đăng nhập được bình thường.
        assertThat(callLogin("10.0.0.3").getStatus()).isEqualTo(200);
    }

    @Test
    void actuatorKhongBiGioiHan() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        for (int i = 0; i < 50; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
            request.setRemoteAddr("10.0.0.4");
            filter.doFilter(request, new MockHttpServletResponse(), chain);
        }
        // Không request nào bị chặn: healthcheck của Docker và Prometheus scrape
        // phải luôn đi lọt, nếu không container sẽ bị đánh dấu unhealthy.
        verify(chain, times(50)).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void docSanPham_dungHanMucGlobal_khongBiChanO6LanDau() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        for (int i = 0; i < 6; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
            request.setRemoteAddr("10.0.0.5");
            filter.doFilter(request, new MockHttpServletResponse(), chain);
        }
        verify(chain, times(6)).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
