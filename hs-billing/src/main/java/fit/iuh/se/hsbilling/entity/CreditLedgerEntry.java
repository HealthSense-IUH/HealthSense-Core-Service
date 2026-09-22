package fit.iuh.se.hsbilling.entity;

import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "credit_ledger_entries")
@Getter
@org.hibernate.annotations.Immutable
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreditLedgerEntry extends BaseEntity {
    @Id @SnowflakeGenerated
    @Column(nullable = false, updatable = false) Long id;
    @Column(name = "wallet_id", nullable = false) Long walletId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) CreditOperation operation;
    @Column(nullable = false) long quantity;
    @Column(name = "delta_balance", nullable = false) long deltaBalance;
    @Column(name = "delta_reserved", nullable = false) long deltaReserved;
    @Column(name = "balance_after", nullable = false) long balanceAfter;
    @Column(name = "reserved_after", nullable = false) long reservedAfter;
    @Enumerated(EnumType.STRING) @Column(name = "source_type", nullable = false, length = 30) CreditSourceType sourceType;
    @Column(name = "source_id", nullable = false) Long sourceId;
    @Column(name = "related_entry_id") Long relatedEntryId;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 180) String idempotencyKey;
    @Column(name = "actor_id") Long actorId;
    @Column(length = 500) String reason;
}
