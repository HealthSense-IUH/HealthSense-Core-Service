package fit.iuh.se.hsapplication.controller.chat;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.AcceptCareServiceAgreementRequest;
import fit.iuh.se.hschat.dto.request.RequestConsultationRenewalRequest;
import fit.iuh.se.hschat.dto.response.CareServiceAgreementResponse;
import fit.iuh.se.hschat.dto.response.ConsultationPaymentResponse;
import fit.iuh.se.hschat.dto.response.ConsultationRenewalResponse;
import fit.iuh.se.hschat.dto.response.SessionExtensionResponse;
import fit.iuh.se.hschat.service.agreement.CareServiceAgreementService;
import fit.iuh.se.hschat.service.payment.ConsultationPaymentService;
import fit.iuh.se.hschat.service.renewal.ConsultationRenewalService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationRenewalController {

    ConsultationRenewalService renewalService;
    CareServiceAgreementService agreementService;
    ConsultationPaymentService paymentService;

    @PostMapping("/api/consultation-sessions/{sessionId}/renewals")
    public ApiResponse<ConsultationRenewalResponse> request(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId,
            @RequestBody(required = false) RequestConsultationRenewalRequest request) {
        return new ApiResponse<>(renewalService.request(currentUser.getUserId(), sessionId, request));
    }

    @GetMapping("/api/consultation-sessions/{sessionId}/renewals")
    public ApiResponse<List<ConsultationRenewalResponse>> history(
            @AuthenticationPrincipal UserAuthentication currentUser, @PathVariable Long sessionId) {
        return new ApiResponse<>(renewalService.getMemberSessionRenewals(currentUser.getUserId(), sessionId));
    }

    @PatchMapping("/api/consultation-renewals/{renewalId}/cancel")
    public ApiResponse<ConsultationRenewalResponse> cancel(
            @AuthenticationPrincipal UserAuthentication currentUser, @PathVariable Long renewalId) {
        return new ApiResponse<>(renewalService.cancel(currentUser.getUserId(), renewalId));
    }

    @GetMapping("/api/consultation-sessions/{sessionId}/extensions")
    public ApiResponse<List<SessionExtensionResponse>> extensions(
            @AuthenticationPrincipal UserAuthentication currentUser, @PathVariable Long sessionId) {
        return new ApiResponse<>(renewalService.getMemberSessionExtensions(currentUser.getUserId(), sessionId));
    }

    @GetMapping("/api/consultation-renewals/{renewalId}/agreement")
    public ApiResponse<CareServiceAgreementResponse> agreement(
            @AuthenticationPrincipal UserAuthentication currentUser, @PathVariable Long renewalId) {
        return new ApiResponse<>(agreementService.getRenewalAgreement(currentUser.getUserId(), renewalId));
    }

    @PostMapping("/api/consultation-renewals/{renewalId}/agreement/accept")
    public ApiResponse<CareServiceAgreementResponse> acceptAgreement(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long renewalId,
            @Valid @RequestBody AcceptCareServiceAgreementRequest request) {
        return new ApiResponse<>(agreementService.acceptRenewal(
                currentUser.getUserId(), renewalId, request.getAgreementId()));
    }

    @PostMapping("/api/consultation-renewals/{renewalId}/payment")
    public ApiResponse<ConsultationPaymentResponse> createPayment(
            @AuthenticationPrincipal UserAuthentication currentUser, @PathVariable Long renewalId) {
        return new ApiResponse<>(paymentService.createRenewalPayment(currentUser.getUserId(), renewalId));
    }

    @GetMapping("/api/consultation-renewals/{renewalId}/payment/attempts")
    public ApiResponse<List<ConsultationPaymentResponse>> paymentAttempts(
            @AuthenticationPrincipal UserAuthentication currentUser, @PathVariable Long renewalId) {
        return new ApiResponse<>(paymentService.getRenewalPaymentAttempts(currentUser.getUserId(), renewalId));
    }
}
