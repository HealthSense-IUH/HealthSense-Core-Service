package fit.iuh.se.hsapplication.controller;

import fit.iuh.se.hsauth.dto.request.LoginRequest;
import fit.iuh.se.hsauth.dto.request.MobileLogoutRequest;
import fit.iuh.se.hsauth.dto.request.MobileRefreshRequest;
import fit.iuh.se.hsauth.dto.response.MobileLoginResponse;
import fit.iuh.se.hsauth.service.auth.AuthService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/mobile")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MobileAuthController {

    AuthService authService;

    @PostMapping("/login")
    public ApiResponse<MobileLoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return new ApiResponse<>(authService.mobileLogin(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<MobileLoginResponse> refresh(@Valid @RequestBody MobileRefreshRequest request) {
        return new ApiResponse<>(authService.mobileRefresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody MobileLogoutRequest request) {
        authService.mobileLogout(request);
        return new ApiResponse<>();
    }
}

