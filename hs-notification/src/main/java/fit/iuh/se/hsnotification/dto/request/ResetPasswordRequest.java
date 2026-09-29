package fit.iuh.se.hsnotification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ResetPasswordRequest {

    @NotBlank(message = "{validation.reset-token-required}")
    String resetToken;

    @NotBlank(message = "{validation.password-required}")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "{validation.password-min-8-letters-numbers}"
    )
    String newPassword;
}
