package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitConsultationMoreInfoRequest {

    Long healthRecordId;

    @Size(max = 1000, message = "Thông tin bổ sung không được vượt quá 1000 ký tự")
    String additionalNote;
}
