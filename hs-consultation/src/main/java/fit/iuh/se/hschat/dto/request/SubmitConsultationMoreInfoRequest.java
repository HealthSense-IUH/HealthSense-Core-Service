package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitConsultationMoreInfoRequest {

    Long healthRecordId;

    @Size(max = 1000, message = "{validation.additional-note-max-1000}")
    String additionalNote;

    @Size(max = 2000, message = "{validation.additional-response-max-2000}")
    String responseNote;

    List<Long> selectedHealthRecordIds;
}
