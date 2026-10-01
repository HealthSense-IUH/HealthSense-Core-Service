package fit.iuh.se.hsapplication.controller.billing;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsbilling.dto.CreditLedgerResponse;
import fit.iuh.se.hsbilling.dto.CreditPackageResponse;
import fit.iuh.se.hsbilling.dto.CreditWalletResponse;
import fit.iuh.se.hsbilling.dto.MemberCreditPaymentOverview;
import fit.iuh.se.hsbilling.service.ConsultationCreditService;
import fit.iuh.se.hsbilling.service.CreditPurchaseService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.Instant;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
public class ConsultationCreditController {
    private final ConsultationCreditService credits;
    private final CreditPurchaseService purchases;

    @GetMapping("/packages")
    public ApiResponse<List<CreditPackageResponse>> packages(@AuthenticationPrincipal UserAuthentication actor) {
        return new ApiResponse<>(credits.getPackages(member(actor)));
    }

    @GetMapping("/wallet")
    public ApiResponse<CreditWalletResponse> wallet(@AuthenticationPrincipal UserAuthentication actor) {
        return new ApiResponse<>(credits.getWallet(member(actor)));
    }

    @GetMapping("/payments/overview")
    public ApiResponse<MemberCreditPaymentOverview> paymentOverview(
            @AuthenticationPrincipal UserAuthentication actor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return new ApiResponse<>(purchases.getPaymentOverview(member(actor), from, to));
    }

    @GetMapping("/ledger")
    public ApiResponse<PageResponse<CreditLedgerResponse>> ledger(@AuthenticationPrincipal UserAuthentication actor,
                                                                  @RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam(defaultValue = "10") int size) {
        if (page < 1 || size < 1 || size > 100)
            throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.page-size-range", "100");
        return new ApiResponse<>(credits.getLedger(member(actor), PageRequest.of(page - 1, size)));
    }

    private Long member(UserAuthentication actor) {
        if (actor == null) throw new AppException(ErrorCode.UNAUTHORIZED);
        if (actor.getRole() != UserRole.MEMBER) throw new AppException(ErrorCode.ACCESS_DENIED);
        return actor.getUserId();
    }
}
