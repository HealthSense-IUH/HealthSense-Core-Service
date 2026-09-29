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

    @NotBlank(message = "{validation.email-required}")
    @Email(message = "{validation.email-invalid}")
    @Size(max = 255, message = "{validation.email-max-255}")
    String email;

    @NotNull(message = "{validation.role-required}")
    UserRole role;

    @NotBlank(message = "{validation.display-name-required}")
    @Size(max = 120, message = "{validation.display-name-max-120}")
    String displayName;

    @Size(max = 30, message = "{validation.phone-max-30}")
    String phone;

    LocalDate dateOfBirth;

    @Size(max = 20, message = "{validation.gender-max-20}")
    String gender;

    @Size(max = 500, message = "{validation.address-max-500}")
    String address;
}
