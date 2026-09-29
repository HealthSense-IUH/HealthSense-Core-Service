package fit.iuh.se.hsnotification.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class VerifyForgotPasswordOtpRequest {

    @NotBlank(message = "{validation.email-required}")
    @Email(message = "{validation.email-invalid}")
    String email;

    @NotBlank(message = "{validation.otp-required}")
    @Pattern(regexp = "\\d{6}", message = "{validation.otp-6-digits}")
    String otp;
}
