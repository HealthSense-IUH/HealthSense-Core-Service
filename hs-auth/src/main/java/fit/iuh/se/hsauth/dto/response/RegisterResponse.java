package fit.iuh.se.hsauth.dto.response;

import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RegisterResponse {
    Long userId;
    String email;
    String fullName;
    UserRole role;
    AccountStatus accountStatus;
}