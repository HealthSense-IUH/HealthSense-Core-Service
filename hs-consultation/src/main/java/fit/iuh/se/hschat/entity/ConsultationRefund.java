package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationPaymentProvider;
import fit.iuh.se.hschat.entity.enums.ConsultationRefundStatus;
import fit.iuh.se.hschat.entity.enums.RefundRecommendation;
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
@Table(name = "consultation_refunds", indexes = {
        @Index(name = "idx_refund_status_updated", columnList = "status, updated_at"),
        @Index(name = "idx_refund_member_created", columnList = "member_id, created_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_refund_payment", columnNames = "payment_id")
})
public class ConsultationRefund extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(nullable = false, updatable = false)
    Long id;

    @Column(name = "payment_id", nullable = false, updatable = false)
    Long paymentId;

    @Column(name = "request_id", updatable = false)
    Long requestId;

    @Column(name = "renewal_id", updatable = false)
    Long renewalId;

    @Column(name = "session_id", updatable = false)
    Long sessionId;

    @Column(name = "agreement_id", nullable = false, updatable = false)
    Long agreementId;

    @Column(name = "member_id", nullable = false, updatable = false)
    Long memberId;

    @Column(name = "original_paid_amount", nullable = false, updatable = false, precision = 14, scale = 2)
    BigDecimal originalPaidAmount;

    @Column(name = "currency", nullable = false, updatable = false, length = 3)
    String currency;

    @Column(name = "refund_policy_reference", nullable = false, updatable = false, length = 255)
    String refundPolicyReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    ConsultationRefundStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", length = 20)
    RefundRecommendation recommendation;

    @Column(name = "recommended_amount", precision = 14, scale = 2)
    BigDecimal recommendedAmount;

    @Column(name = "review_reason", length = 1000)
    String reviewReason;

    @Column(name = "operational_context", length = 4000)
    String operationalContext;

    @Column(name = "reviewed_by")
    Long reviewedBy;

    @Column(name = "reviewed_at")
    Instant reviewedAt;

    @Column(name = "approved_amount", precision = 14, scale = 2)
    BigDecimal approvedAmount;

    @Column(name = "decision_reason", length = 1000)
    String decisionReason;

    @Column(name = "decided_by")
    Long decidedBy;

    @Column(name = "decided_at")
    Instant decidedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    ConsultationPaymentProvider provider;

    @Column(name = "provider_refund_id", length = 150)
    String providerRefundId;

    @Column(name = "provider_result", length = 1000)
    String providerResult;

    @Column(name = "execution_attempts", nullable = false)
    @Builder.Default
    Integer executionAttempts = 0;

    @Column(name = "last_execution_at")
    Instant lastExecutionAt;

    @Column(name = "completed_at")
    Instant completedAt;

    @Version
    @Column(nullable = false)
    long version;
}
