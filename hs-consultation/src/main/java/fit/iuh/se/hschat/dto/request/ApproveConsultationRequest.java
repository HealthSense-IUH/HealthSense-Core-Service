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

    @NotNull(message = "{validation.doctor-id-required}")
    Long doctorId;
}
