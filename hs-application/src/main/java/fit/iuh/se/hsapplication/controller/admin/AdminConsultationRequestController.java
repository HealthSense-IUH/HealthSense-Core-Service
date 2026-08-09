package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.ApproveConsultationRequest;
import fit.iuh.se.hschat.dto.request.RejectConsultationRequest;
import fit.iuh.se.hschat.dto.request.RequestMoreConsultationInfoRequest;
import fit.iuh.se.hschat.dto.response.ConsultationRequestReviewResponse;
import fit.iuh.se.hschat.dto.response.ConsultationRequestResponse;
import fit.iuh.se.hschat.dto.response.DoctorCandidateResponse;
import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import fit.iuh.se.hschat.service.request.ConsultationRequestService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Validated
@RestController
@RequestMapping("/api/admin/consultation-requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminConsultationRequestController {

    ConsultationRequestService consultationRequestService;

    @GetMapping
    public ApiResponse<PageResponse<ConsultationRequestResponse>> getRequestsForAdmin(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(name = "status", required = false) ConsultationRequestStatus status,
            @RequestParam(name = "memberId", required = false) Long memberId,
            @RequestParam(name = "preferredDoctorId", required = false) Long preferredDoctorId,
            @RequestParam(name = "assignedDoctorId", required = false) Long assignedDoctorId,
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new ApiResponse<>(consultationRequestService.getRequestsForAdmin(
                currentUser.getRole(),
                status,
                memberId,
                preferredDoctorId,
                assignedDoctorId,
                fromDate,
                toDate,
                pageable
        ));
    }

    @GetMapping("/{requestId}")
    public ApiResponse<ConsultationRequestReviewResponse> getRequestReviewById(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long requestId) {
        return new ApiResponse<>(consultationRequestService.getRequestReviewById(currentUser.getRole(), requestId));
    }

    @GetMapping("/{requestId}/doctor-candidates")
    public ApiResponse<PageResponse<DoctorCandidateResponse>> getDoctorCandidates(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long requestId,
            @RequestParam(name = "specialty", required = false) DoctorSpecialty specialty,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "eligibleOnly", required = false) Boolean eligibleOnly,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id"));
        return new ApiResponse<>(consultationRequestService.getDoctorCandidates(
                currentUser.getRole(),
                requestId,
                specialty,
                keyword,
                eligibleOnly,
                pageable
        ));
    }

    @PatchMapping("/{requestId}/approve")
    public ApiResponse<ConsultationRequestResponse> approveRequest(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long requestId,
            @Valid @RequestBody ApproveConsultationRequest request) {
        return new ApiResponse<>(consultationRequestService.approveRequest(currentUser.getUserId(), currentUser.getRole(), requestId, request));
    }

    @PatchMapping("/{requestId}/reject")
    public ApiResponse<ConsultationRequestResponse> rejectRequest(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long requestId,
            @Valid @RequestBody RejectConsultationRequest request) {
        return new ApiResponse<>(consultationRequestService.rejectRequest(currentUser.getUserId(), currentUser.getRole(), requestId, request));
    }

    @PatchMapping("/{requestId}/need-more-info")
    public ApiResponse<ConsultationRequestResponse> requestMoreInfo(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long requestId,
            @Valid @RequestBody RequestMoreConsultationInfoRequest request) {
        return new ApiResponse<>(consultationRequestService.requestMoreInfo(
                currentUser.getUserId(),
                currentUser.getRole(),
                requestId,
                request
        ));
    }

    @PostMapping("/expire-waiting-payment")
    public ApiResponse<Void> expireWaitingPaymentRequests(
            @AuthenticationPrincipal UserAuthentication currentUser) {
        consultationRequestService.expireWaitingPaymentRequests(currentUser.getRole());
        return new ApiResponse<>();
    }
}
