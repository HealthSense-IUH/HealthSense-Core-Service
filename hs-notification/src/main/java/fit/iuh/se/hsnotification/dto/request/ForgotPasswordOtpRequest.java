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

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    String email;
}
