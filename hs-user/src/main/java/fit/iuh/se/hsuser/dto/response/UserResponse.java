package fit.iuh.se.hsuser.dto.response;

import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {

    Long id;
    String email;
    UserRole role;
    AccountStatus status;
    String displayName;
    String phone;
    LocalDate dateOfBirth;
    String gender;
    String avatarUrl;
    String address;
    Instant createdAt;
    Instant updatedAt;
}
