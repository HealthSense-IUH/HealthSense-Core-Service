package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApproveConsultationRequest {

    @NotNull(message = "Id bác sĩ không được để trống")
    Long doctorId;
}
