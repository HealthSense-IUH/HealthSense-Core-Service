package fit.iuh.se.hschat.dto.request;

import fit.iuh.se.hschat.entity.enums.CareTerminationReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CloseConsultationRequest {

    @NotBlank(message = "{validation.close-consultation-reason-required}")
    @Size(max = 500, message = "{validation.close-consultation-reason-max-500}")
    String closeReason;

    CareTerminationReason terminationReason;

    Boolean meaningfulCareOccurred;
}
