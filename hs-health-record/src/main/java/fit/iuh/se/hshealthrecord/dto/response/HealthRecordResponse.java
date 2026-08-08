package fit.iuh.se.hshealthrecord.dto.response;

import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.Instant;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HealthRecordResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    Long userId;
    String fileName;
    Long fileSize;
    RecordStatus status;
    PredictionLabel predictionLabel;
    Double confidence;
    Map<String, Object> hrvFeatures;
    Instant createdAt;
    Instant updatedAt;
}
