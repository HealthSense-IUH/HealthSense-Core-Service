package fit.iuh.se.hsapplication.controller;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.CreateConsultationRequest;
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
@RequestMapping("/api/consultation-requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationRequestController {

    ConsultationRequestService consultationRequestService;

    @PostMapping
    public ApiResponse<ConsultationRequestResponse> createRequest(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @Valid @RequestBody CreateConsultationRequest request) {
        return new ApiResponse<>(consultationRequestService.createRequest(currentUser.getUserId(), request));
    }
}
