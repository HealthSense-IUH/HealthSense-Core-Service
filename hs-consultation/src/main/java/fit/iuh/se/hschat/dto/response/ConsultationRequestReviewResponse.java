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
public class ConsultationRequestReviewResponse {

    Long id;
    ConsultationRequestStatus status;
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
    Long packageId;
    BigDecimal packagePriceSnapshot;
    Integer packageDurationDaysSnapshot;
    UserSummaryResponse member;
    UserSummaryResponse preferredDoctor;
    UserSummaryResponse assignedDoctor;
    HealthRecordSummaryResponse healthRecord;
    String moreInfoReason;
    String memberAdditionalNote;
    Long assignedDoctorId;
    Instant doctorReservedAt;
    Instant paymentDeadline;
    Instant createdAt;
    Instant updatedAt;
}
