package fit.iuh.se.hsauth.entity;

import fit.iuh.se.hsauth.dto.response.LoginResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AuthenticationResult{
    LoginResponse response;
    String refreshToken;
    String sessionId;
}
