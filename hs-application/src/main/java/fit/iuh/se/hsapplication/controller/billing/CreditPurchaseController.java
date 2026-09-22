package fit.iuh.se.hsapplication.controller.billing;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsbilling.dto.*;
import fit.iuh.se.hsbilling.service.CreditPurchaseService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.*;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credits/orders")
@RequiredArgsConstructor
public class CreditPurchaseController {
    private final CreditPurchaseService purchases;

    @PostMapping
    public ApiResponse<CreditOrderResponse> create(@AuthenticationPrincipal UserAuthentication actor,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody CreateCreditOrderRequest request) {
        Long memberId = member(actor);
        if (key == null || !key.matches("[A-Za-z0-9._:-]{1,128}") || request.packageId() == null || request.packageId() <= 0)
            throw new AppException(ErrorCode.INVALID_PARAMETER, "A positive packageId and valid Idempotency-Key are required");
        return new ApiResponse<>(purchases.createOrder(memberId, request.packageId(), key));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<CreditOrderResponse> get(@AuthenticationPrincipal UserAuthentication actor, @PathVariable Long orderId) {
        return new ApiResponse<>(purchases.getOrder(member(actor), orderId));
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<CreditOrderResponse> cancel(@AuthenticationPrincipal UserAuthentication actor,
            @PathVariable Long orderId) {
        return new ApiResponse<>(purchases.cancelOrder(member(actor), orderId));
    }

    @GetMapping
    public ApiResponse<PageResponse<CreditOrderSummary>> list(@AuthenticationPrincipal UserAuthentication actor,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        Long memberId = member(actor);
        if (page < 1 || size < 1 || size > 100) throw new AppException(ErrorCode.INVALID_PARAMETER);
        return new ApiResponse<>(purchases.getOrders(memberId, PageRequest.of(page - 1, size)));
    }

    private Long member(UserAuthentication actor) {
        if (actor == null) throw new AppException(ErrorCode.UNAUTHORIZED);
        if (actor.getRole() != UserRole.MEMBER) throw new AppException(ErrorCode.ACCESS_DENIED);
        return actor.getUserId();
    }
}
