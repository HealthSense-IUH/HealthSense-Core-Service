package fit.iuh.se.hsauth.service.session;

import fit.iuh.se.hsauth.entity.AuthSession;

import java.time.Duration;
import java.util.Optional;

public interface AuthSessionService {

    void save(AuthSession session, Duration ttl);

    Optional<AuthSession> findById(Long userId, String sessionId);

    void updateRefreshTokenHash(Long userId, String sessionId, String refreshTokenHash, Duration ttl);

    void revoke(Long userId, String sessionId);
}
