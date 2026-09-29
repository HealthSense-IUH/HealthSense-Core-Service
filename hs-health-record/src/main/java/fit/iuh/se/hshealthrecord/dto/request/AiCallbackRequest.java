package fit.iuh.se.hshealthrecord.dto.request;

import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiCallbackRequest {

    @NotNull(message = "{validation.record-id-required}")
    Long recordId;

    @NotNull(message = "{validation.prediction-label-required}")
    PredictionLabel predictionLabel;

    Double confidence;

    String hrvFeaturesJson;
}
