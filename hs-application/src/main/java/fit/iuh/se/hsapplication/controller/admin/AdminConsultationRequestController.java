package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.ApproveConsultationRequest;
import fit.iuh.se.hschat.dto.request.RejectConsultationRequest;
import fit.iuh.se.hschat.dto.response.ConsultationRequestResponse;
import fit.iuh.se.hschat.service.consultation.ConsultationRequestService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @GetMapping
    public ApiResponse<PageResponse<ConsultationRequestResponse>> getRequestsForAdmin(
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new ApiResponse<>(consultationRequestService.getRequestsForAdmin(pageable));
    }

    @PatchMapping("/{requestId}/approve")
    public ApiResponse<ConsultationRequestResponse> approveRequest(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long requestId,
            @Valid @RequestBody ApproveConsultationRequest request) {
        return new ApiResponse<>(consultationRequestService.approveRequest(currentUser.getUserId(), requestId, request));
    }

    @PatchMapping("/{requestId}/reject")
    public ApiResponse<ConsultationRequestResponse> rejectRequest(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long requestId,
            @Valid @RequestBody RejectConsultationRequest request) {
        return new ApiResponse<>(consultationRequestService.rejectRequest(currentUser.getUserId(), requestId, request));
    }
}
