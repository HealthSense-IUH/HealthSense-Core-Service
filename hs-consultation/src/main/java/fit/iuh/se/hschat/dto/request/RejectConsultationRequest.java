package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RejectConsultationRequest {

    @NotBlank(message = "{validation.rejection-reason-required}")
    @Size(max = 500, message = "{validation.rejection-reason-max-500}")
    String rejectionReason;
}
