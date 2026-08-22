package fit.iuh.se.hsuser.dto.request;

import fit.iuh.se.hsuser.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserCreateRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email;

    @NotNull(message = "Role is required")
    UserRole role;

    @NotBlank(message = "Display name is required")
    @Size(max = 120, message = "Display name must not exceed 120 characters")
    String displayName;

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    String phone;

    LocalDate dateOfBirth;

    @Size(max = 20, message = "Gender must not exceed 20 characters")
    String gender;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    String address;
}
