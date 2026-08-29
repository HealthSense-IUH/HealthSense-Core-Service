package fit.iuh.se.hsapplication.controller.doctor;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.UpsertConsultationFinalSummaryRequest;
import fit.iuh.se.hschat.dto.request.CreateFinalSummaryAddendumRequest;
import fit.iuh.se.hschat.dto.response.ConsultationFinalSummaryResponse;
import fit.iuh.se.hschat.dto.response.FinalSummaryAddendumResponse;
import fit.iuh.se.hschat.dto.response.DoctorConsultationDetailResponse;
import fit.iuh.se.hschat.dto.response.DoctorConsultationSessionResponse;
import fit.iuh.se.hschat.dto.response.DoctorScopedHealthRecordResponse;
import fit.iuh.se.hschat.dto.response.RawHealthRecordArtifactResponse;
import fit.iuh.se.hschat.dto.response.CareContinuitySummaryResponse;
import fit.iuh.se.hschat.service.carehistory.CareHistoryService;
import fit.iuh.se.hschat.service.activecare.DoctorActiveCareService;
import fit.iuh.se.hschat.service.finalsummary.ConsultationFinalSummaryService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
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

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/doctor/consultation-sessions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorActiveCareController {

    DoctorActiveCareService doctorActiveCareService;
    ConsultationFinalSummaryService finalSummaryService;
    CareHistoryService careHistoryService;

    @GetMapping
    public ApiResponse<PageResponse<DoctorConsultationSessionResponse>> getAssignedSessions(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        validateDoctor(currentUser);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "lastMessageAt"));
        return new ApiResponse<>(doctorActiveCareService.getAssignedSessions(currentUser.getUserId(), pageable));
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<DoctorConsultationDetailResponse> getSessionDetail(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId) {
        validateDoctor(currentUser);
        return new ApiResponse<>(doctorActiveCareService.getSessionDetail(currentUser.getUserId(), sessionId));
    }

    @GetMapping("/{sessionId}/health-records")
    public ApiResponse<PageResponse<DoctorScopedHealthRecordResponse>> getScopedHealthRecords(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        validateDoctor(currentUser);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new ApiResponse<>(doctorActiveCareService.getScopedHealthRecords(currentUser.getUserId(), sessionId, pageable));
    }

    @GetMapping("/{sessionId}/health-records/{recordId}")
    public ApiResponse<DoctorScopedHealthRecordResponse> getScopedHealthRecord(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId,
            @PathVariable Long recordId) {
        validateDoctor(currentUser);
        return new ApiResponse<>(doctorActiveCareService.getScopedHealthRecord(currentUser.getUserId(), sessionId, recordId));
    }

    @GetMapping("/{sessionId}/health-records/{recordId}/raw-artifact")
    public ApiResponse<RawHealthRecordArtifactResponse> getRawArtifact(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId,
            @PathVariable Long recordId) {
        validateDoctor(currentUser);
        return new ApiResponse<>(doctorActiveCareService.getRawArtifact(
                currentUser.getUserId(), sessionId, recordId));
    }

    @GetMapping("/{sessionId}/continuity-summaries")
    public ApiResponse<List<CareContinuitySummaryResponse>> getContinuitySummaries(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId) {
        validateDoctor(currentUser);
        return new ApiResponse<>(careHistoryService.getContinuitySummaries(
                currentUser.getUserId(), sessionId));
    }

    @PatchMapping("/{sessionId}/health-records/{recordId}/attention/review")
    public ApiResponse<DoctorScopedHealthRecordResponse> markAttentionReviewed(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId,
            @PathVariable Long recordId) {
        validateDoctor(currentUser);
        return new ApiResponse<>(doctorActiveCareService.markAttentionReviewed(currentUser.getUserId(), sessionId, recordId));
    }

    @GetMapping("/{sessionId}/final-summary")
    public ApiResponse<ConsultationFinalSummaryResponse> getFinalSummary(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId) {
        validateDoctor(currentUser);
        return new ApiResponse<>(finalSummaryService.getForDoctor(currentUser.getUserId(), sessionId));
    }

    @PutMapping("/{sessionId}/final-summary")
    public ApiResponse<ConsultationFinalSummaryResponse> upsertFinalSummaryDraft(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId,
            @Valid @RequestBody UpsertConsultationFinalSummaryRequest request) {
        validateDoctor(currentUser);
        return new ApiResponse<>(finalSummaryService.upsertDraft(currentUser.getUserId(), sessionId, request));
    }

    @PatchMapping("/{sessionId}/final-summary/finalize")
    public ApiResponse<ConsultationFinalSummaryResponse> finalizeSummary(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId) {
        validateDoctor(currentUser);
        return new ApiResponse<>(finalSummaryService.finalizeSummary(currentUser.getUserId(), sessionId));
    }

    @PostMapping("/{sessionId}/final-summary/addenda")
    public ApiResponse<FinalSummaryAddendumResponse> createFinalSummaryAddendum(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId,
            @Valid @RequestBody CreateFinalSummaryAddendumRequest request) {
        validateDoctor(currentUser);
        return new ApiResponse<>(finalSummaryService.createAddendum(
                currentUser.getUserId(), sessionId, request));
    }

    private void validateDoctor(UserAuthentication currentUser) {
        if (currentUser.getRole() != UserRole.DOCTOR)
            throw new AppException(ErrorCode.ACCESS_DENIED);
    }
}
