package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationRenewalStatus;
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
public class ConsultationRenewalResponse {
    Long id;
    Long sessionId;
    Long memberId;
    Long doctorId;
    Long packageFamilyId;
    Long packageId;
    Integer packageVersion;
    Integer durationDays;
    BigDecimal priceAmount;
    String currency;
    String supportScheduleSnapshotJson;
    String supportTimezoneSnapshot;
    Instant previousEndsAt;
    Instant proposedNewEndsAt;
    Long agreementId;
    Long successfulPaymentId;
    ConsultationRenewalStatus status;
    Instant requestedAt;
    Long reviewedBy;
    Instant reviewStartedAt;
    Instant reviewedAt;
    String rejectionReason;
    Instant paymentDeadline;
    Instant appliedAt;
    Instant createdAt;
    Instant updatedAt;
}
