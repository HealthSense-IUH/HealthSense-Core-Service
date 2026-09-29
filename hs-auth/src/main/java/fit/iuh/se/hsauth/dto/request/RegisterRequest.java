package fit.iuh.se.hsauth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RegisterRequest {

    @NotBlank(message = "{validation.email-required}")
    @Email(message = "{validation.email-invalid}")
    String email;

    @NotBlank(message = "{validation.password-required}")
    @Size(min = 8, message = "{validation.password-min-8}")
    String password;

    @NotBlank(message = "{validation.full-name-required}")
    @Size(max = 120, message = "{validation.full-name-max-120}")
    String fullName;
}
