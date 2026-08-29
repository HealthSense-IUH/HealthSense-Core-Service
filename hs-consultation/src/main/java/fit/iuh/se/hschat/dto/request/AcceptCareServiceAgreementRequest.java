package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AcceptCareServiceAgreementRequest {

    @NotNull
    Long agreementId;

    @AssertTrue(message = "Agreement acceptance must be explicit")
    boolean accepted;
}
