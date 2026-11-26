package fit.iuh.se.hsapplication.config.security;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsapplication.dto.ratelimit.RateLimitResult;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitFilterTest {

    private RateLimiterService rateLimiterService;
    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        rateLimiterService = mock(RateLimiterService.class);
        filter = new RateLimitFilter(rateLimiterService, JsonMapper.builder().build());
        ReflectionTestUtils.setField(filter, "enabled", true);
        ReflectionTestUtils.setField(filter, "assistantCapacity", 20L);
        ReflectionTestUtils.setField(filter, "assistantWindowSeconds", 60L);
        ReflectionTestUtils.setField(filter, "authCapacity", 15L);
        ReflectionTestUtils.setField(filter, "authWindowSeconds", 60L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ignoresSpoofedXForwardedForHeaderAndUsesRemoteAddr() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/public/ping");
        request.setRemoteAddr("10.0.0.5");
        request.addHeader("X-Forwarded-For", "1.2.3.4");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        when(rateLimiterService.consume("ip:10.0.0.5"))
                .thenReturn(new RateLimitResult(true, 100L, 99L, 60L));

        filter.doFilter(request, response, chain);

        verify(rateLimiterService).consume("ip:10.0.0.5");
        verify(chain).doFilter(request, response);
    }

    @Test
    void appliesStricterAuthBucketOnLoginAndForgotPassword() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/mobile/login");
        request.setRemoteAddr("10.0.0.9");
        request.addHeader("X-Forwarded-For", "9.9.9.9");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        when(rateLimiterService.consume("auth:ip:10.0.0.9", 15L, 60L))
                .thenReturn(new RateLimitResult(false, 15L, 0L, 45L));

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("retry-after")).isEqualTo("45");
        verify(rateLimiterService).consume("auth:ip:10.0.0.9", 15L, 60L);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void skipsActuatorHealthProbe() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health/readiness");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(rateLimiterService, never()).consume(org.mockito.ArgumentMatchers.anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    void appliesDedicatedBucketForAssistantStreamAndReturns429JsonWhenBlocked() throws Exception {
        UserAuthentication principal = new UserAuthentication(42L, "member@test.local", UserRole.MEMBER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/assistant/conversations/c1/stream");
        request.addHeader("lang", "en");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        when(rateLimiterService.consume("assistant:user:42", 20L, 60L))
                .thenReturn(new RateLimitResult(false, 20L, 0L, 15L));

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("retry-after")).isEqualTo("15");
        assertThat(response.getContentAsString()).contains("\"code\":429");
        verify(chain, never()).doFilter(request, response);
    }
}
