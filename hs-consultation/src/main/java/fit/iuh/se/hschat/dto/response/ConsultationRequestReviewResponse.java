package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
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
public class ConsultationRequestReviewResponse {

    Long id;
    ConsultationRequestStatus status;
    String reason;
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
