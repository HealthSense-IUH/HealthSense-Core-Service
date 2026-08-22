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

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    String email;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "\\d{6}", message = "OTP must contain 6 digits")
    String otp;
}
