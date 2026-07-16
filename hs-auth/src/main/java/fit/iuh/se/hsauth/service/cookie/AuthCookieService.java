package fit.iuh.se.hsauth.service.cookie;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

import java.util.Optional;

public interface AuthCookieService {

    Optional<String> getCookieValue(HttpServletRequest request, String name);

    ResponseCookie createRefreshTokenCookie(String refreshToken);

    ResponseCookie createSessionIdCookie(String sessionId);

    ResponseCookie clearRefreshTokenCookie();

    ResponseCookie clearSessionIdCookie();
}
