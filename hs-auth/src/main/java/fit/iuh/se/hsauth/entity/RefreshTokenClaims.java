package fit.iuh.se.hsauth.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RefreshTokenClaims {
    Long userId;
    String sessionId;
    String tokenId;
}
