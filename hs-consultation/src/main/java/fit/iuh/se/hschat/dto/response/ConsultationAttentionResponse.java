package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationAttentionReason;
import fit.iuh.se.hschat.entity.enums.ConsultationAttentionStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsultationAttentionResponse {
    Long id;
    Long sessionId;
    Long healthRecordId;
    ConsultationAttentionStatus status;
    ConsultationAttentionReason reason;
    Instant reviewedAt;
    Long reviewedByDoctorId;
    Instant createdAt;
    Instant updatedAt;
}
