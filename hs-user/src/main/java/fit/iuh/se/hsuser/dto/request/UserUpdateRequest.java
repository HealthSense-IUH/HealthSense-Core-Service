package fit.iuh.se.hsuser.dto.request;

import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
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
public class UserUpdateRequest {

    @Email(message = "Email is invalid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email;

    UserRole role;

    AccountStatus status;

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
