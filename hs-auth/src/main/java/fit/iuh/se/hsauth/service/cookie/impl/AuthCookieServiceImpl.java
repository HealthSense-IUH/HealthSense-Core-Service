package fit.iuh.se.hsauth.service.cookie.impl;

import fit.iuh.se.hsauth.entity.enums.CookieType;
import fit.iuh.se.hsauth.service.cookie.AuthCookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthCookieServiceImpl implements AuthCookieService {

    @NonFinal
    @Value("${security.cookie.path}")
    String cookiePath;

    @NonFinal
    @Value("${security.cookie.secure:false}")
    boolean secure;

    @NonFinal
    @Value("${security.cookie.same-site:Lax}")
    String sameSite;

    @NonFinal
    @Value("${security.jwt.refresh-token-ttl}")
    Duration refreshTokenTtl;

    @Override
    public Optional<String> getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    @Override
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return buildCookie(CookieType.REFRESH_TOKEN.getType(), refreshToken, refreshTokenTtl);
    }

    @Override
    public ResponseCookie createSessionIdCookie(String sessionId) {
        return buildCookie(CookieType.SESSION_ID.getType(), sessionId, refreshTokenTtl);
    }

    @Override
    public ResponseCookie clearRefreshTokenCookie() {
        return buildCookie(CookieType.REFRESH_TOKEN.getType(), "", Duration.ZERO);
    }

    @Override
    public ResponseCookie clearSessionIdCookie() {
        return buildCookie(CookieType.SESSION_ID.getType(), "", Duration.ZERO);
    }

    private ResponseCookie buildCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(cookiePath)
                .maxAge(maxAge)
                .build();
    }
}
