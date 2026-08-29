package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.entity.enums.FinalSummaryClosureStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CareHistoryEpisodeResponse {
    Long sessionId;
    Long doctorId;
    Long packageId;
    Integer packageVersion;
    Instant startedAt;
    Instant activatedAt;
    Instant endsAt;
    ConsultationStatus status;
    FinalSummaryClosureStatus summaryClosureStatus;
    Instant summaryDueAt;
    Instant summaryEscalatedAt;
    String summaryEscalationReason;
    ConsultationFinalSummaryResponse finalizedSummary;
    List<EpisodeHealthRecordAuthorizationResponse> healthRecords;
}
