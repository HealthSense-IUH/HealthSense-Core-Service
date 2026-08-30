package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DecideConsultationRenewalRequest {
    @NotNull
    Boolean approved;
    @Size(max = 1000)
    String rejectionReason;
}
