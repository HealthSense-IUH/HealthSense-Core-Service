package fit.iuh.se.hsnotification.dto.command;

import java.time.Instant;

public record CreditPurchaseReceiptMailCommand(
        String recipientEmail,
        String displayName,
        Long orderId,
        String packageCode,
        String packageName,
        long creditQuantity,
        long amountVnd,
        String currency,
        String provider,
        Instant paidAt,
        long walletBalance,
        long walletReserved,
        long walletAvailable
) {}
