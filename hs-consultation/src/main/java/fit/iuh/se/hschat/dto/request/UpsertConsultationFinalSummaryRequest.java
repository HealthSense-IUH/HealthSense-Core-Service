package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpsertConsultationFinalSummaryRequest {

    @Size(max = 10000, message = "{validation.summary-content-max-10000}")
    String summary;

    @Size(max = 10000, message = "{validation.observations-max-10000}")
    String observations;

    @Size(max = 10000, message = "{validation.recommendations-max-10000}")
    String recommendations;

    @Size(max = 10000, message = "{validation.follow-up-recommendation-max-10000}")
    String followUpRecommendation;

    java.util.Set<Long> referencedHealthRecordIds;

    public UpsertConsultationFinalSummaryRequest(
            String summary,
            String observations,
            String recommendations,
            String followUpRecommendation) {
        this.summary = summary;
        this.observations = observations;
        this.recommendations = recommendations;
        this.followUpRecommendation = followUpRecommendation;
    }
}
