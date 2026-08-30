package fit.iuh.se.hsapplication.controller.chat;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.response.ConsultationPaymentResponse;
import fit.iuh.se.hschat.service.payment.ConsultationPaymentService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultation-requests/{requestId}/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationPaymentController {

    ConsultationPaymentService consultationPaymentService;

    @PostMapping
    public ApiResponse<ConsultationPaymentResponse> createPayment(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long requestId) {
        return new ApiResponse<>(consultationPaymentService.createPayment(currentUser.getUserId(), requestId));
    }

    @GetMapping
    public ApiResponse<ConsultationPaymentResponse> getPayment(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long requestId) {
        return new ApiResponse<>(consultationPaymentService.getPayment(currentUser.getUserId(), requestId));
    }

    @GetMapping("/attempts")
    public ApiResponse<List<ConsultationPaymentResponse>> getPaymentAttempts(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long requestId) {
        return new ApiResponse<>(consultationPaymentService.getPaymentAttempts(currentUser.getUserId(), requestId));
    }
}
