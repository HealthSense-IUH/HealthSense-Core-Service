package fit.iuh.se.hsapplication.config.security;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsapplication.dto.ratelimit.RateLimitResult;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.i18n.ErrorMessages;
import fit.iuh.se.hsshared.i18n.RequestLanguage;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import tools.jackson.databind.ObjectMapper;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String RATE_LIMIT_LIMIT = "ratelimit-limit";
    private static final String RATE_LIMIT_REMAINING = "ratelimit-remaining";
    private static final String RETRY_AFTER = "retry-after";

    final RateLimiterService rateLimiterService;
    final ObjectMapper objectMapper;

    @Value("${app.rate-limit.enabled}")
    boolean enabled;

    @Value("${app.assistant.rate-limit.capacity:20}")
    long assistantCapacity;

    @Value("${app.assistant.rate-limit.window-seconds:60}")
    long assistantWindowSeconds;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!enabled || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        return uri != null && (uri.equals("/actuator/health") || uri.startsWith("/actuator/health/"));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String actorKey = resolveKey(request);
        RateLimitResult result = isAssistantStreamRequest(request)
                ? rateLimiterService.consume("assistant:" + actorKey, assistantCapacity, assistantWindowSeconds)
                : rateLimiterService.consume(actorKey);

        response.setHeader(RATE_LIMIT_LIMIT, String.valueOf(result.getLimit()));
        response.setHeader(RATE_LIMIT_REMAINING, String.valueOf(result.getRemaining()));

        if (result.isAllowed()) {
            filterChain.doFilter(request, response);
            return;
        }

        ErrorCode errorCode = ErrorCode.TOO_MANY_REQUESTS;
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(RETRY_AFTER, String.valueOf(result.getRetryAfterSeconds()));
        objectMapper.writeValue(response.getWriter(), new ApiResponse<>(errorCode.getCode(), ErrorMessages.code(errorCode, RequestLanguage.resolve(request))));
    }

    private boolean isAssistantStreamRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return "POST".equalsIgnoreCase(request.getMethod())
                && uri != null
                && uri.startsWith("/api/assistant/conversations/")
                && uri.endsWith("/stream");
    }

    private String resolveKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserAuthentication user) {
            return "user:" + user.getUserId();
        }
        return "ip:" + resolveClientIp(request);
    }

    /**
     * Dùng remoteAddr đã được Spring ForwardedHeaderFilter chuẩn hóa theo server.forward-headers-strategy=framework,
     * tránh client tự giả mạo header X-Forwarded-For để vượt rate limit.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr == null || remoteAddr.isBlank()) ? "unknown" : remoteAddr;
    }
}
