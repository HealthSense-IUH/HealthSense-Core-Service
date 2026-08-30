package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.*;
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
public class ConsultationRefundResponse {
    Long id;
    Long paymentId;
    Long requestId;
    Long renewalId;
    Long sessionId;
    Long agreementId;
    Long memberId;
    BigDecimal originalPaidAmount;
    String currency;
    String refundPolicyReference;
    ConsultationRefundStatus status;
    RefundRecommendation recommendation;
    BigDecimal recommendedAmount;
    String reviewReason;
    String operationalContext;
    Long reviewedBy;
    Instant reviewedAt;
    BigDecimal approvedAmount;
    String decisionReason;
    Long decidedBy;
    Instant decidedAt;
    ConsultationPaymentProvider provider;
    String providerRefundId;
    String providerResult;
    Integer executionAttempts;
    Instant lastExecutionAt;
    Instant completedAt;
    Instant createdAt;
    Instant updatedAt;
}
