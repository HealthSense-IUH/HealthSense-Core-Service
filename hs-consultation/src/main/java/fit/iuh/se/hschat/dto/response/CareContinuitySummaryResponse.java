package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CareContinuitySummaryResponse {
    Long sessionId;
    Long doctorId;
    Long packageId;
    Integer packageVersion;
    Instant startedAt;
    Instant endsAt;
    ConsultationStatus status;
    ConsultationFinalSummaryResponse finalizedSummary;
}
