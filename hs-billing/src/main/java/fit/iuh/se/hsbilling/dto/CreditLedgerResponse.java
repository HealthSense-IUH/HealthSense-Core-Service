package fit.iuh.se.hsbilling.dto;

import fit.iuh.se.hsbilling.entity.enums.*;
import java.time.Instant;

// Internal idempotency keys, actor IDs and administrative reasons are not exposed here.
public record CreditLedgerResponse(
        String id,
        CreditOperation operation, long quantity, long deltaBalance, long deltaReserved,
        long balanceAfter, long reservedAfter, CreditSourceType sourceType,
        String sourceId, Instant createdAt) {}
