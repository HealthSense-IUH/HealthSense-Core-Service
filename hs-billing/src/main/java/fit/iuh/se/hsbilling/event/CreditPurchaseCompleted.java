package fit.iuh.se.hsbilling.event;

import fit.iuh.se.hsbilling.entity.enums.CreditPaymentProvider;

import java.time.Instant;

public record CreditPurchaseCompleted(
        Long orderId,
        Long memberId,
        String packageCode,
        String packageName,
        long creditQuantity,
        long amountVnd,
        String currency,
        CreditPaymentProvider provider,
        Instant paidAt,
        long walletBalance,
        long walletReserved,
        long walletAvailable
) {}
