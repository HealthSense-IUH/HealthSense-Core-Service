package fit.iuh.se.hsauth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 16/07/2026, Thursday
 **/

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    String email;

    @NotBlank(message = "Password is required")
    String password;
}
