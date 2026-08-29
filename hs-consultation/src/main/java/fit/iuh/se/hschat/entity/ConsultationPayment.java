package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationPaymentProvider;
import fit.iuh.se.hschat.entity.enums.ConsultationPaymentPurpose;
import fit.iuh.se.hschat.entity.enums.ConsultationPaymentStatus;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "consultation_payments",
        indexes = {
                @Index(name = "idx_payment_request", columnList = "request_id"),
                @Index(name = "idx_payment_agreement_status", columnList = "agreement_id, status"),
                @Index(name = "idx_payment_member_status", columnList = "member_id, status"),
                @Index(name = "idx_payment_order_code", columnList = "order_code"),
                @Index(name = "idx_payment_status_expires_at", columnList = "status, expires_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_payment_agreement_attempt", columnNames = {"agreement_id", "attempt_number"}),
                @UniqueConstraint(name = "uq_payment_order_code", columnNames = "order_code"),
                @UniqueConstraint(name = "uq_payment_link_id", columnNames = "payment_link_id")
        }
)
public class ConsultationPayment extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "request_id")
    Long requestId;

    @Column(name = "renewal_id")
    Long renewalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_purpose", nullable = false, length = 30)
    @Builder.Default
    ConsultationPaymentPurpose paymentPurpose = ConsultationPaymentPurpose.INITIAL_CARE;

    @Column(name = "agreement_id", nullable = false)
    Long agreementId;

    @Column(name = "attempt_number", nullable = false)
    Integer attemptNumber;

    @Column(name = "member_id", nullable = false)
    Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    ConsultationPaymentProvider provider;

    @Column(name = "order_code", nullable = false, unique = true)
    Long orderCode;

    @Column(name = "payment_link_id", unique = true, length = 100)
    String paymentLinkId;

    @Column(name = "checkout_url", length = 1000)
    String checkoutUrl;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    ConsultationPaymentStatus status;

    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;

    @Column(name = "paid_at")
    Instant paidAt;

    @Column(name = "expired_at")
    Instant expiredAt;

    @Column(name = "cancelled_at")
    Instant cancelledAt;
}
