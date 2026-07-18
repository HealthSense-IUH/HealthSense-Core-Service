package fit.iuh.se.hsapplication.config.security;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsapplication.dto.ratelimit.RateLimitResult;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
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

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !enabled || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String key = resolveKey(request);
        RateLimitResult result = rateLimiterService.consume(key);

        response.setHeader(RATE_LIMIT_LIMIT, String.valueOf(result.limit()));
        response.setHeader(RATE_LIMIT_REMAINING, String.valueOf(result.remaining()));

        if (result.allowed()) {
            filterChain.doFilter(request, response);
            return;
        }

        ErrorCode errorCode = ErrorCode.TOO_MANY_REQUESTS;
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(RETRY_AFTER, String.valueOf(result.retryAfterSeconds()));
        objectMapper.writeValue(response.getWriter(), new ApiResponse<>(errorCode.getCode(), errorCode.getMessage()));
    }

    private String resolveKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserAuthentication user) {
            return "user:" + user.getUserId();
        }
        return "ip:" + resolveClientIp(request);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String forwarded = request.getHeader("Forwarded");
        if (forwarded != null && !forwarded.isBlank()) {
            for (String part : forwarded.split(";")) {
                String trimmed = part.trim();
                if (trimmed.toLowerCase().startsWith("for=")) {
                    return trimmed.substring(4).replace("\"", "");
                }
            }
        }

        return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
    }
}
