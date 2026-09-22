package fit.iuh.se.hsbilling.entity;

import fit.iuh.se.hsbilling.entity.enums.CreditOrderStatus;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "credit_purchase_orders", uniqueConstraints =
        @UniqueConstraint(name = "uq_credit_order_member_key", columnNames = {"member_id", "idempotency_key"}))
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class CreditPurchaseOrder extends BaseEntity {
    @Id @SnowflakeGenerated @Column(updatable = false) private Long id;
    @Column(name = "member_id", nullable = false, updatable = false) private Long memberId;
    @Column(name = "package_id", nullable = false, updatable = false) private Long packageId;
    @Column(name = "package_code", nullable = false, updatable = false, length = 80) private String packageCode;
    @Column(name = "package_name", nullable = false, updatable = false, length = 160) private String packageName;
    @Column(name = "credit_quantity", nullable = false, updatable = false) private long creditQuantity;
    @Column(name = "amount_vnd", nullable = false, updatable = false) private long amountVnd;
    @Column(nullable = false, updatable = false, length = 3) private String currency;
    @Setter @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private CreditOrderStatus status;
    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 128) private String idempotencyKey;
    @Column(name = "request_fingerprint", nullable = false, updatable = false, length = 100) private String requestFingerprint;
    @Setter @Column(name = "paid_at") private Instant paidAt;
}
