package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HealthRecordSummaryResponse {

    Long recordId;
    RecordStatus status;
    PredictionLabel predictionLabel;
    Double confidence;
    Instant createdAt;
    Instant updatedAt;
}
