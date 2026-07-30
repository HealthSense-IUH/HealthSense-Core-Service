package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    String reason;
    Long preferredDoctorId;
    ConsultationRequestStatus status;
    Long assignedDoctorId;
    Long consultationSessionId;
    Long reviewedByAdminId;
    Instant reviewedAt;
    String rejectionReason;
    Instant createdAt;
    Instant updatedAt;
}
