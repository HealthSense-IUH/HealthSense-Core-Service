package fit.iuh.se.hshealthrecord.event;

import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class HealthRecordAnalyzedEvent {
    Long recordId;
    Long userId;
    PredictionLabel predictionLabel;
    Double confidence;
    Instant analyzedAt;
}
