package fit.iuh.se.hsapplication.controller.chat;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.response.ConsultationFinalSummaryResponse;
import fit.iuh.se.hschat.dto.response.ConsultationSessionResponse;
import fit.iuh.se.hschat.service.finalsummary.ConsultationFinalSummaryService;
import fit.iuh.se.hschat.service.session.ConsultationSessionService;
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
@RequestMapping("/api/consultation-sessions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationSessionController {

    ConsultationSessionService consultationSessionService;
    ConsultationFinalSummaryService finalSummaryService;

    @GetMapping
    public ApiResponse<PageResponse<ConsultationSessionResponse>> getMySessions(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "lastMessageAt"));
        PageResponse<ConsultationSessionResponse> response = currentUser.getRole() == UserRole.DOCTOR
                ? consultationSessionService.getDoctorSessions(currentUser.getUserId(), pageable)
                : consultationSessionService.getMySessions(currentUser.getUserId(), pageable);
        return new ApiResponse<>(response);
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<ConsultationSessionResponse> getSessionById(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId) {
        return new ApiResponse<>(consultationSessionService.getSessionById(currentUser.getUserId(), sessionId));
    }

    @GetMapping("/{sessionId}/final-summary")
    public ApiResponse<ConsultationFinalSummaryResponse> getFinalSummary(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId) {
        return new ApiResponse<>(finalSummaryService.getForMember(currentUser.getUserId(), sessionId));
    }
}
