package fit.iuh.se.hsapplication.controller.billing;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsbilling.dto.*;
import fit.iuh.se.hsbilling.service.ConsultationCreditService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.*;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
public class ConsultationCreditController {
    private final ConsultationCreditService credits;

    @GetMapping("/packages")
    public ApiResponse<List<CreditPackageResponse>> packages(@AuthenticationPrincipal UserAuthentication actor) {
        return new ApiResponse<>(credits.getPackages(member(actor)));
    }

    @GetMapping("/wallet")
    public ApiResponse<CreditWalletResponse> wallet(@AuthenticationPrincipal UserAuthentication actor) {
        return new ApiResponse<>(credits.getWallet(member(actor)));
    }

    @GetMapping("/ledger")
    public ApiResponse<PageResponse<CreditLedgerResponse>> ledger(@AuthenticationPrincipal UserAuthentication actor,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 1 || size < 1 || size > 100)
            throw new AppException(ErrorCode.INVALID_PARAMETER, "page must be positive and size must be between 1 and 100");
        return new ApiResponse<>(credits.getLedger(member(actor), PageRequest.of(page - 1, size)));
    }

    private Long member(UserAuthentication actor) {
        if (actor == null) throw new AppException(ErrorCode.UNAUTHORIZED);
        if (actor.getRole() != UserRole.MEMBER) throw new AppException(ErrorCode.ACCESS_DENIED);
        return actor.getUserId();
    }
}
