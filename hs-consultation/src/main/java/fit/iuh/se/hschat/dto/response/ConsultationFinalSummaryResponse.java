package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationFinalSummaryStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsultationFinalSummaryResponse {

    Long id;
    Long sessionId;
    ConsultationFinalSummaryStatus status;
    String summary;
    String observations;
    String recommendations;
    String followUpRecommendation;
    Long createdByDoctorId;
    Instant finalizedAt;
    Instant createdAt;
    Instant updatedAt;
}
