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

    @Size(max = 10000, message = "Nội dung tổng kết không được vượt quá 10000 ký tự")
    String summary;

    @Size(max = 10000, message = "Nhận xét không được vượt quá 10000 ký tự")
    String observations;

    @Size(max = 10000, message = "Khuyến nghị không được vượt quá 10000 ký tự")
    String recommendations;

    @Size(max = 10000, message = "Khuyến nghị theo dõi không được vượt quá 10000 ký tự")
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
