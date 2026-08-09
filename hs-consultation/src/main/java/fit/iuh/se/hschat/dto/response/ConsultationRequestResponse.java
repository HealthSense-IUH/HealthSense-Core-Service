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
public class ConsultationRequestResponse {

    Long id;
    Long memberId;
    Long healthRecordId;
    Long packageId;
    BigDecimal packagePriceSnapshot;
    Integer packageDurationDaysSnapshot;
    String reason;
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
