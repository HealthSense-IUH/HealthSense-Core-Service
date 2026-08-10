package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.AdminCreateConsultationSessionRequest;
import fit.iuh.se.hschat.dto.request.CloseConsultationRequest;
import fit.iuh.se.hschat.dto.request.ExtendConsultationRequest;
import fit.iuh.se.hschat.dto.response.ConsultationFinalSummaryResponse;
import fit.iuh.se.hschat.dto.response.ConsultationSessionResponse;
import fit.iuh.se.hschat.service.finalsummary.ConsultationFinalSummaryService;
import fit.iuh.se.hschat.service.session.ConsultationSessionService;
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
@RequestMapping("/api/admin/consultation-sessions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminConsultationSessionController {

    ConsultationSessionService consultationSessionService;
    ConsultationFinalSummaryService finalSummaryService;

    @PostMapping
    public ApiResponse<ConsultationSessionResponse> createSessionByAdmin(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestBody @Valid AdminCreateConsultationSessionRequest request) {
        return new ApiResponse<>(consultationSessionService.createSessionByAdmin(currentUser.getUserId(), currentUser.getRole(), request));
    }

    @GetMapping
    public ApiResponse<PageResponse<ConsultationSessionResponse>> getSessionsForAdmin(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new ApiResponse<>(consultationSessionService.getSessionsForAdmin(currentUser.getRole(), pageable));
    }

    @PatchMapping("/{sessionId}/extend")
    public ApiResponse<ConsultationSessionResponse> extendSession(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId,
            @RequestBody @Valid ExtendConsultationRequest request) {
        return new ApiResponse<>(consultationSessionService.extendSession(currentUser.getUserId(), currentUser.getRole(), sessionId, request));
    }

    @PatchMapping("/{sessionId}/close")
    public ApiResponse<ConsultationSessionResponse> closeSession(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId,
            @RequestBody @Valid CloseConsultationRequest request) {
        return new ApiResponse<>(consultationSessionService.closeSession(currentUser.getUserId(), currentUser.getRole(), sessionId, request));
    }

    @GetMapping("/{sessionId}/final-summary")
    public ApiResponse<ConsultationFinalSummaryResponse> getFinalSummary(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId) {
        return new ApiResponse<>(finalSummaryService.getForAdmin(currentUser.getRole(), sessionId));
    }

    @PostMapping("/expire-overdue")
    public ApiResponse<Void> expireOverdueSessions(
            @AuthenticationPrincipal UserAuthentication currentUser) {
        consultationSessionService.expireOverdueSessions(currentUser.getRole());
        return new ApiResponse<>();
    }

    @PostMapping("/activate-scheduled")
    public ApiResponse<Void> activateScheduledSessions(
            @AuthenticationPrincipal UserAuthentication currentUser) {
        consultationSessionService.activateScheduledSessions(currentUser.getRole());
        return new ApiResponse<>();
    }
}
