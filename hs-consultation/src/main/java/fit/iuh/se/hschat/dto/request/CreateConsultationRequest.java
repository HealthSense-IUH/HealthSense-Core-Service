package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateConsultationRequest {

    @Deprecated
    Long packageId;

    Long healthRecordId;

    @Deprecated
    String reason;

    @NotBlank(message = "{validation.care-reason-required}")
    @Size(max = 1000, message = "{validation.care-reason-max-1000}")
    String reasonForCare;

    @NotBlank(message = "{validation.current-concern-required}")
    @Size(max = 2000, message = "{validation.current-concern-max-2000}")
    String currentConcern;

    @Size(max = 1000, message = "{validation.care-goal-max-1000}")
    String careGoal;

    @Size(max = 1000, message = "{validation.member-note-max-1000}")
    String memberNote;

    @Size(max = 4000, message = "{validation.self-reported-context-max-4000}")
    String relevantSelfReportedContext;

    List<Long> selectedHealthRecordIds;

    @Deprecated
    Long preferredDoctorId;
}
