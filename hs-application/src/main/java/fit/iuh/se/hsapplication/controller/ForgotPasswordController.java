package fit.iuh.se.hsapplication.controller;

import fit.iuh.se.hsnotification.dto.request.ForgotPasswordOtpRequest;
import fit.iuh.se.hsnotification.dto.request.ResetPasswordRequest;
import fit.iuh.se.hsnotification.dto.request.VerifyForgotPasswordOtpRequest;
import fit.iuh.se.hsnotification.dto.response.ForgotPasswordOtpResponse;
import fit.iuh.se.hsnotification.dto.response.VerifyForgotPasswordOtpResponse;
import fit.iuh.se.hsnotification.service.PasswordRecoveryService;
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
@RequestMapping("/api/auth/forgot-password")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ForgotPasswordController {

    PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/request-otp")
    public ApiResponse<ForgotPasswordOtpResponse> requestOtp(
            @Valid @RequestBody ForgotPasswordOtpRequest request) {
        return new ApiResponse<>(passwordRecoveryService.requestOtp(request));
    }

    @PostMapping("/verify-otp")
    public ApiResponse<VerifyForgotPasswordOtpResponse> verifyOtp(
            @Valid @RequestBody VerifyForgotPasswordOtpRequest request) {
        return new ApiResponse<>(passwordRecoveryService.verifyOtp(request));
    }

    @PostMapping("/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordRecoveryService.resetPassword(request);
        return new ApiResponse<>(null);
    }
}
