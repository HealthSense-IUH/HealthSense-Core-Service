package fit.iuh.se.hsapplication.controller.chat;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.CreateConsultationRequest;
import fit.iuh.se.hschat.dto.response.ConsultationRequestResponse;
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

    @GetMapping
    public ApiResponse<PageResponse<ConsultationRequestResponse>> getMyRequests(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new ApiResponse<>(consultationRequestService.getMyRequests(currentUser.getUserId(), pageable));
    }

    @PatchMapping("/{requestId}/cancel")
    public ApiResponse<ConsultationRequestResponse> cancelMyRequest(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long requestId) {
        return new ApiResponse<>(consultationRequestService.cancelMyRequest(currentUser.getUserId(), requestId));
    }

    @GetMapping("/{requestId}")
    public ApiResponse<ConsultationRequestResponse> getMyRequestById(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long requestId) {
        return new ApiResponse<>(consultationRequestService.getMyRequestById(currentUser.getUserId(), requestId));
    }
}
