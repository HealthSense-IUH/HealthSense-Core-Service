package fit.iuh.se.hsauth.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AuthSession{
    Long userId;
    String sessionId;
    String refreshTokenHash;
    Instant createdAt;
    Instant expiresAt;
}
