package fit.iuh.se.hsauth.dto.token;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordResetTokenClaims {
    Long userId;
    String email;
    String tokenId;
}
