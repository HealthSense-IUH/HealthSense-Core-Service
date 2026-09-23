package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsbilling.dto.*;
import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsbilling.service.CreditAdministrationService;
import fit.iuh.se.hschat.service.refund.ConsultationCreditRefundService;
import fit.iuh.se.hschat.service.recovery.ConsultationCreditRecoveryService;
import fit.iuh.se.hschat.dto.response.CreditRecoveryResponse;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.*;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin/credits")
@RequiredArgsConstructor
public class AdminCreditController {
    private final CreditAdministrationService admin;
    private final ConsultationCreditRefundService refunds;
    private final ConsultationCreditRecoveryService recovery;

    @PostMapping("/packages")
    public ApiResponse<AdminCreditPackageResponse> createPackage(@AuthenticationPrincipal UserAuthentication actor,
            @RequestBody AdminCreditPackageRequest request) {
        require(actor); return new ApiResponse<>(admin.createPackage(actor.getUserId(), actor.getRole(), request));
    }

    @PatchMapping("/packages/{id}")
    public ApiResponse<AdminCreditPackageResponse> updatePackage(@AuthenticationPrincipal UserAuthentication actor,
            @PathVariable Long id, @RequestBody AdminCreditPackageRequest request) {
        require(actor); return new ApiResponse<>(admin.updatePackage(actor.getUserId(), actor.getRole(), id, request));
    }

    @GetMapping("/packages")
    public ApiResponse<List<AdminCreditPackageResponse>> packages(@AuthenticationPrincipal UserAuthentication actor) {
        require(actor); return new ApiResponse<>(admin.getPackages(actor.getRole()));
    }

    @GetMapping("/members")
    public ApiResponse<PageResponse<AdminMemberCreditSummary>> members(@AuthenticationPrincipal UserAuthentication actor,
            @RequestParam(required=false) AccountStatus status, @RequestParam(required=false) String keyword,
            @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int size) {
        require(actor);
        PageRequest pageable = page(page,size).withSort(Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return new ApiResponse<>(admin.getMembers(actor.getUserId(), actor.getRole(), status, keyword, pageable));
    }

    @GetMapping("/wallets/{memberId}")
    public ApiResponse<CreditWalletResponse> wallet(@AuthenticationPrincipal UserAuthentication actor,
            @PathVariable Long memberId) {
        require(actor); return new ApiResponse<>(admin.getWallet(actor.getRole(), memberId));
    }

    @GetMapping("/wallets/{memberId}/ledger")
    public ApiResponse<PageResponse<AdminCreditLedgerResponse>> ledger(@AuthenticationPrincipal UserAuthentication actor,
            @PathVariable Long memberId, @RequestParam(required=false) CreditOperation operation,
            @RequestParam(required=false) CreditSourceType sourceType,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int size) {
        require(actor); return new ApiResponse<>(admin.getLedger(actor.getRole(), memberId, operation, sourceType,
                from, to, page(page,size)));
    }

    @GetMapping("/orders")
    public ApiResponse<PageResponse<AdminCreditOrderSummary>> orders(@AuthenticationPrincipal UserAuthentication actor,
            @RequestParam(required=false) Long memberId, @RequestParam(required=false) CreditOrderStatus status,
            @RequestParam(required=false) CreditPaymentProvider provider,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int size) {
        require(actor); return new ApiResponse<>(admin.getOrders(actor.getRole(),memberId,status,provider,from,to,page(page,size)));
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<AdminCreditOrderResponse> order(@AuthenticationPrincipal UserAuthentication actor,
            @PathVariable Long orderId) {
        require(actor); return new ApiResponse<>(admin.getOrder(actor.getRole(),orderId));
    }

    @PostMapping("/wallets/{memberId}/adjustments")
    public ApiResponse<CreditMutationResponse> adjust(@AuthenticationPrincipal UserAuthentication actor,
            @PathVariable Long memberId, @RequestHeader(value="Idempotency-Key",required=false) String key,
            @RequestBody CreditAdjustmentRequest request) {
        require(actor); if(request==null||request.delta()==null) throw new AppException(ErrorCode.INVALID_PARAMETER);
        return new ApiResponse<>(admin.adjust(actor.getUserId(),actor.getRole(),memberId,request.delta(),request.reason(),key));
    }

    @PostMapping("/session-refunds")
    public ApiResponse<CreditMutationResponse> refund(@AuthenticationPrincipal UserAuthentication actor,
            @RequestHeader(value="Idempotency-Key",required=false) String key,
            @RequestBody SessionCreditRefundRequest request) {
        require(actor); if(request==null) throw new AppException(ErrorCode.INVALID_PARAMETER);
        return new ApiResponse<>(refunds.refund(actor.getUserId(),actor.getRole(),request.sessionId(),request.reason(),key));
    }

    @GetMapping("/reconciliation/wallets/{memberId}")
    public ApiResponse<CreditWalletReconciliationResponse> reconcile(@AuthenticationPrincipal UserAuthentication actor,
            @PathVariable Long memberId) {
        require(actor); return new ApiResponse<>(admin.reconcile(actor.getRole(),memberId));
    }

    @PostMapping("/recovery")
    public ApiResponse<CreditRecoveryResponse> recover(@AuthenticationPrincipal UserAuthentication actor,
            @RequestParam(defaultValue="50") int limit) {
        require(actor); return new ApiResponse<>(recovery.recover(actor.getUserId(),actor.getRole(),limit));
    }

    private PageRequest page(int page,int size) {
        if(page<1||size<1||size>100) throw new AppException(ErrorCode.INVALID_PARAMETER,
                "page must be positive and size must be between 1 and 100");
        return PageRequest.of(page-1,size);
    }
    private void require(UserAuthentication actor) { if(actor==null) throw new AppException(ErrorCode.UNAUTHORIZED); }
}
