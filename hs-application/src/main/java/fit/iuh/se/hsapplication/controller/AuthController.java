package fit.iuh.se.hsapplication.controller;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsauth.dto.request.LoginRequest;
import fit.iuh.se.hsauth.dto.request.RegisterRequest;
import fit.iuh.se.hsauth.dto.response.LoginResponse;
import fit.iuh.se.hsauth.dto.response.RegisterResponse;
import fit.iuh.se.hsauth.dto.token.AuthenticationResult;
import fit.iuh.se.hsauth.entity.enums.CookieType;
import fit.iuh.se.hsauth.service.auth.AuthService;
import fit.iuh.se.hsauth.service.cookie.AuthCookieService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;
    AuthCookieService authCookieService;

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ApiResponse<>(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthenticationResult result = authService.login(request);
        setAuthCookies(response, result);
        return new ApiResponse<>(result.getResponse());
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = authCookieService.getCookieValue(request, CookieType.REFRESH_TOKEN.getType())
                .orElse(null);
        String sessionId = authCookieService.getCookieValue(request, CookieType.SESSION_ID.getType())
                .orElse(null);

        AuthenticationResult result = authService.refresh(refreshToken, sessionId);
        setAuthCookies(response, result);
        return new ApiResponse<>(result.getResponse());
    }

    @PostMapping("/refresh/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = authCookieService.getCookieValue(request, CookieType.REFRESH_TOKEN.getType())
                .orElse(null);
        String sessionId = authCookieService.getCookieValue(request, CookieType.SESSION_ID.getType())
                .orElse(null);

        authService.logout(refreshToken, sessionId);
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.clearRefreshTokenCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.clearSessionIdCookie().toString());
        return new ApiResponse<>(null);
    }

    @GetMapping("/me")
    public ApiResponse<UserAuthentication> me(@AuthenticationPrincipal UserAuthentication user) {
        return new ApiResponse<>(user);
    }

    private void setAuthCookies(HttpServletResponse response, AuthenticationResult result) {
        ResponseCookie refreshTokenCookie = authCookieService.createRefreshTokenCookie(result.getRefreshToken());
        ResponseCookie sessionIdCookie = authCookieService.createSessionIdCookie(result.getSessionId());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, sessionIdCookie.toString());
    }
}
