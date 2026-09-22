package fit.iuh.se.hsbilling.dto;

import fit.iuh.se.hsbilling.entity.CreditPurchaseOrder;
import fit.iuh.se.hsbilling.entity.enums.CreditOrderStatus;
import java.time.Instant;

public record AdminCreditOrderSummary(String memberId, String id, String packageId, String packageCode,
        String packageName, long creditQuantity, long amountVnd, String currency,
        CreditOrderStatus status, Instant createdAt, Instant paidAt) {
    public static AdminCreditOrderSummary from(CreditPurchaseOrder order) {
        return new AdminCreditOrderSummary(order.getMemberId().toString(), order.getId().toString(),
                order.getPackageId().toString(), order.getPackageCode(), order.getPackageName(),
                order.getCreditQuantity(), order.getAmountVnd(), order.getCurrency(), order.getStatus(),
                order.getCreatedAt(), order.getPaidAt());
    }
}
