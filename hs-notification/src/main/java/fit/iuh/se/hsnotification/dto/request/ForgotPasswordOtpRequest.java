package fit.iuh.se.hsnotification.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ForgotPasswordOtpRequest {

    @NotBlank(message = "{validation.email-required}")
    @Email(message = "{validation.email-invalid}")
    String email;
}
