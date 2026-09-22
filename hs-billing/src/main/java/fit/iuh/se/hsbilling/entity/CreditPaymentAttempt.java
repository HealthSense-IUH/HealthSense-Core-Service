package fit.iuh.se.hsbilling.entity;

import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "credit_payment_attempts", uniqueConstraints = {
    @UniqueConstraint(name = "uq_credit_attempt_number", columnNames = {"order_id", "attempt_number"}),
    @UniqueConstraint(name = "uq_credit_attempt_reference", columnNames = {"provider", "provider_reference"})
})
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class CreditPaymentAttempt extends BaseEntity {
    @Id @SnowflakeGenerated @Column(updatable = false) private Long id;
    @Column(name = "order_id", nullable = false, updatable = false) private Long orderId;
    @Column(name = "attempt_number", nullable = false, updatable = false) private int attemptNumber;
    @Enumerated(EnumType.STRING) @Column(nullable = false, updatable = false, length = 20) private CreditPaymentProvider provider;
    @Setter @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private CreditPaymentStatus status;
    @Setter @Column(name = "order_code") private Long orderCode;
    @Setter @Column(name = "payment_link_id", length = 160) private String paymentLinkId;
    @Setter @Column(name = "checkout_url", length = 1000) private String checkoutUrl;
    @Setter @Column(name = "expires_at") private Instant expiresAt;
    @Setter @Column(name = "cancelled_at") private Instant cancelledAt;
    @Setter @Column(name = "last_error", length = 1000) private String lastError;
    @Setter @Column(name = "provider_reference", length = 160) private String providerReference;
    @Setter @Column(name = "verified_amount_vnd") private Long verifiedAmountVnd;
    @Setter @Column(name = "verified_currency", length = 3) private String verifiedCurrency;
    @Setter @Column(name = "paid_at") private Instant paidAt;
}
