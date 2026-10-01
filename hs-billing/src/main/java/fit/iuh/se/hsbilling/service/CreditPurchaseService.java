package fit.iuh.se.hsbilling.service;

import fit.iuh.se.hsbilling.dto.CreditOrderResponse;
import fit.iuh.se.hsbilling.dto.CreditOrderSummary;
import fit.iuh.se.hsbilling.dto.MemberCreditPaymentOverview;
import fit.iuh.se.hsbilling.entity.enums.CreditOrderStatus;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface CreditPurchaseService {
    CreditOrderResponse createOrder(Long memberId, Long packageId, String idempotencyKey);

    CreditOrderResponse getOrder(Long memberId, Long orderId);

    CreditOrderResponse cancelOrder(Long memberId, Long orderId);

    PageResponse<CreditOrderSummary> getOrders(Long memberId, Pageable pageable);

    PageResponse<CreditOrderSummary> getOrders(Long memberId, CreditOrderStatus status,
                                               Instant from, Instant to, Pageable pageable);

    MemberCreditPaymentOverview getPaymentOverview(Long memberId, Instant from, Instant to);
}
