package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.*;
import fit.iuh.se.hschat.dto.response.ConsultationRefundResponse;
import fit.iuh.se.hschat.service.refund.ConsultationRefundService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/consultation-refunds")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminConsultationRefundController {

    ConsultationRefundService refundService;

    @PostMapping("/payments/{paymentId}/recommendation")
    public ApiResponse<ConsultationRefundResponse> recommend(
            @AuthenticationPrincipal UserAuthentication actor,
            @PathVariable Long paymentId,
            @Valid @RequestBody RecommendRefundRequest request) {
        return new ApiResponse<>(refundService.recommend(
                actor.getUserId(), actor.getRole(), paymentId, request));
    }

    @PostMapping("/{refundId}/decision")
    public ApiResponse<ConsultationRefundResponse> decide(
            @AuthenticationPrincipal UserAuthentication actor,
            @PathVariable Long refundId,
            @Valid @RequestBody DecideRefundRequest request) {
        return new ApiResponse<>(refundService.decide(
                actor.getUserId(), actor.getRole(), refundId, request));
    }

    @PostMapping("/{refundId}/execute")
    public ApiResponse<ConsultationRefundResponse> execute(
            @AuthenticationPrincipal UserAuthentication actor, @PathVariable Long refundId) {
        return new ApiResponse<>(refundService.execute(actor.getUserId(), actor.getRole(), refundId));
    }

    @PostMapping("/{refundId}/reconcile")
    public ApiResponse<ConsultationRefundResponse> reconcile(
            @AuthenticationPrincipal UserAuthentication actor,
            @PathVariable Long refundId,
            @Valid @RequestBody ReconcileRefundRequest request) {
        return new ApiResponse<>(refundService.reconcile(
                actor.getUserId(), actor.getRole(), refundId, request));
    }

    @GetMapping("/{refundId}")
    public ApiResponse<ConsultationRefundResponse> get(
            @AuthenticationPrincipal UserAuthentication actor, @PathVariable Long refundId) {
        return new ApiResponse<>(refundService.get(actor.getRole(), refundId));
    }
}
