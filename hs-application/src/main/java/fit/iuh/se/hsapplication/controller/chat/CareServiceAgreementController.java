package fit.iuh.se.hsapplication.controller.chat;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.AcceptCareServiceAgreementRequest;
import fit.iuh.se.hschat.dto.response.CareServiceAgreementResponse;
import fit.iuh.se.hschat.service.agreement.CareServiceAgreementService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultation-requests/{requestId}/agreement")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CareServiceAgreementController {

    CareServiceAgreementService agreementService;

    @GetMapping
    public ApiResponse<CareServiceAgreementResponse> getCurrentAgreement(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long requestId
    ) {
        return new ApiResponse<>(agreementService.getCurrentForMember(currentUser.getUserId(), requestId));
    }

    @PostMapping("/accept")
    public ApiResponse<CareServiceAgreementResponse> acceptAgreement(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long requestId,
            @Valid @RequestBody AcceptCareServiceAgreementRequest request
    ) {
        return new ApiResponse<>(agreementService.accept(
                currentUser.getUserId(), requestId, request.getAgreementId()));
    }
}
