package fit.iuh.se.hsbilling.event;

import fit.iuh.se.hsbilling.entity.enums.CreditPaymentProvider;

public record CreditPurchaseCompleted(Long orderId, Long memberId, long creditQuantity,
        CreditPaymentProvider provider) {}
