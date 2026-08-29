package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.service.payment.PaymentCancellationService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/payment-reconciliation")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminPaymentReconciliationController {
    PaymentCancellationService cancellationService;

    @PostMapping("/{paymentId}/retry-cancellation")
    public ApiResponse<Void> retryCancellation(
            @AuthenticationPrincipal UserAuthentication actor, @PathVariable Long paymentId) {
        cancellationService.reconcileProviderCancellation(
                actor.getUserId(), actor.getRole(), paymentId);
        return new ApiResponse<>();
    }
}
