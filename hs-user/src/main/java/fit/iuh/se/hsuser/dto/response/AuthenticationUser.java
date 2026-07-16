package fit.iuh.se.hsuser.dto.response;

import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationUser {
    Long id;
    String email;
    String passwordHash;
    UserRole role;
    AccountStatus accountStatus;
}
