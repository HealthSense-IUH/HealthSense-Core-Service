package fit.iuh.se.hsbilling.service;

import fit.iuh.se.hsbilling.dto.*;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CreditPurchaseService {
    CreditOrderResponse createOrder(Long memberId, Long packageId, String idempotencyKey);
    CreditOrderResponse getOrder(Long memberId, Long orderId);
    CreditOrderResponse cancelOrder(Long memberId, Long orderId);
    PageResponse<CreditOrderSummary> getOrders(Long memberId, Pageable pageable);
}
