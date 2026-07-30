package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.ApproveConsultationRequest;
import fit.iuh.se.hschat.dto.response.ConsultationRequestResponse;
import fit.iuh.se.hschat.service.consultation.ConsultationRequestService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/consultation-requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminConsultationRequestController {

    ConsultationRequestService consultationRequestService;

    @PatchMapping("/{requestId}/approve")
    public ApiResponse<ConsultationRequestResponse> approveRequest(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long requestId,
            @Valid @RequestBody ApproveConsultationRequest request) {
        return new ApiResponse<>(consultationRequestService.approveRequest(currentUser.getUserId(), requestId, request));
    }
}
