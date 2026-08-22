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

    @NotBlank(message = "Reset token is required")
    String resetToken;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "Password must be at least 8 characters and include letters and numbers"
    )
    String newPassword;
}
