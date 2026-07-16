package fit.iuh.se.hsauth.controller;

import fit.iuh.se.hsauth.dto.request.LoginRequest;
import fit.iuh.se.hsauth.dto.request.RegisterRequest;
import fit.iuh.se.hsauth.dto.response.LoginResponse;
import fit.iuh.se.hsauth.dto.response.RegisterResponse;
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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ApiResponse<>(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return new ApiResponse<>(authService.login(request));
    }
}
