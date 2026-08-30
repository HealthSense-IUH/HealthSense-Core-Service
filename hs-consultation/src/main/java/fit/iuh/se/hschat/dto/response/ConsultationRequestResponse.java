package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsultationRequestResponse {

    Long id;
    Long memberId;
    Long healthRecordId;
    Long packageId;
    Integer packageVersion;
    BigDecimal packagePriceSnapshot;
    Integer packageDurationDaysSnapshot;
    String reason;
    String reasonForCare;
    String currentConcern;
    String careGoal;
    String memberNote;
    String relevantSelfReportedContext;
    List<Long> selectedHealthRecordIds;
    List<HealthRecordSummaryResponse> selectedHealthRecords;
    Instant intakeFrozenAt;
    List<ConsultationMoreInfoCycleResponse> moreInfoHistory;
    Long preferredDoctorId;
    ConsultationRequestStatus status;
    Long assignedDoctorId;
    Instant doctorReservedAt;
    Instant paymentDeadline;
    Long consultationSessionId;
    Long reviewedByAdminId;
    Instant reviewedAt;
    String rejectionReason;
    String moreInfoReason;
    String memberAdditionalNote;
    Instant cancelledAt;
    Instant expiredAt;
    Instant createdAt;
    Instant updatedAt;
}
