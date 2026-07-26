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

    @NotNull(message = "ID bản ghi không được để trống")
    Long recordId;

    @NotNull(message = "Nhãn chẩn đoán không được để trống")
    PredictionLabel predictionLabel;

    Double confidence;

    String hrvFeaturesJson;
}
