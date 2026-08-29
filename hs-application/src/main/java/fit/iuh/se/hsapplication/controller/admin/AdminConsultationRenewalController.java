package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.DecideConsultationRenewalRequest;
import fit.iuh.se.hschat.dto.response.ConsultationRenewalResponse;
import fit.iuh.se.hschat.service.renewal.ConsultationRenewalService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/consultation-renewals/{renewalId}")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminConsultationRenewalController {

    ConsultationRenewalService renewalService;

    @PatchMapping("/begin-review")
    public ApiResponse<ConsultationRenewalResponse> beginReview(
            @AuthenticationPrincipal UserAuthentication currentUser, @PathVariable Long renewalId) {
        return new ApiResponse<>(renewalService.beginReview(
                currentUser.getUserId(), currentUser.getRole(), renewalId));
    }

    @PatchMapping("/decision")
    public ApiResponse<ConsultationRenewalResponse> decide(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long renewalId,
            @Valid @RequestBody DecideConsultationRenewalRequest request) {
        return new ApiResponse<>(renewalService.decide(
                currentUser.getUserId(), currentUser.getRole(), renewalId, request));
    }
}
