package fit.iuh.se.hschat.dto.request;

import fit.iuh.se.hschat.entity.enums.CareTerminationReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestSessionTerminationRequest {

    @NotNull
    CareTerminationReason reason;

    @NotBlank
    @Size(max = 500)
    String details;
}
