package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationFinalSummaryStatus;
import fit.iuh.se.hschat.entity.enums.FinalSummaryClosureStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.Set;

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
    FinalSummaryClosureStatus closureStatus;
    Instant summaryDueAt;
    Set<Long> referencedHealthRecordIds;
    List<FinalSummaryAddendumResponse> addenda;
}
