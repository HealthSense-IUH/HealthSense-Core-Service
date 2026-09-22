package fit.iuh.se.hsbilling.dto;

import fit.iuh.se.hsbilling.entity.CreditLedgerEntry;
import fit.iuh.se.hsbilling.entity.enums.*;
import java.time.Instant;

public record AdminCreditLedgerResponse(String id, CreditOperation operation, long quantity,
        long deltaBalance, long deltaReserved, long balanceAfter, long reservedAfter,
        CreditSourceType sourceType, String sourceId, String relatedEntryId, String actorId,
        String reason, Instant createdAt) {
    public static AdminCreditLedgerResponse from(CreditLedgerEntry e) {
        return new AdminCreditLedgerResponse(e.getId().toString(), e.getOperation(), e.getQuantity(),
                e.getDeltaBalance(), e.getDeltaReserved(), e.getBalanceAfter(), e.getReservedAfter(),
                e.getSourceType(), e.getSourceId().toString(),
                e.getRelatedEntryId() == null ? null : e.getRelatedEntryId().toString(),
                e.getActorId() == null ? null : e.getActorId().toString(), e.getReason(), e.getCreatedAt());
    }
}
