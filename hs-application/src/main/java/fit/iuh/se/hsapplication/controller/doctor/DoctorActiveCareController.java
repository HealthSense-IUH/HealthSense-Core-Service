package fit.iuh.se.hsapplication.controller.doctor;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.response.DoctorConsultationDetailResponse;
import fit.iuh.se.hschat.dto.response.DoctorConsultationSessionResponse;
import fit.iuh.se.hschat.dto.response.DoctorScopedHealthRecordResponse;
import fit.iuh.se.hschat.service.activecare.DoctorActiveCareService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
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
@RequestMapping("/api/doctor/consultation-sessions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorActiveCareController {

    DoctorActiveCareService doctorActiveCareService;

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

    @PatchMapping("/{sessionId}/health-records/{recordId}/attention/review")
    public ApiResponse<DoctorScopedHealthRecordResponse> markAttentionReviewed(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId,
            @PathVariable Long recordId) {
        validateDoctor(currentUser);
        return new ApiResponse<>(doctorActiveCareService.markAttentionReviewed(currentUser.getUserId(), sessionId, recordId));
    }

    private void validateDoctor(UserAuthentication currentUser) {
        if (currentUser.getRole() != UserRole.DOCTOR)
            throw new AppException(ErrorCode.ACCESS_DENIED);
    }
}
