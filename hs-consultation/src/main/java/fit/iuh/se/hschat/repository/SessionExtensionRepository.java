package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.SessionExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionExtensionRepository extends JpaRepository<SessionExtension, Long> {
    List<SessionExtension> findBySessionIdOrderByAppliedAtAsc(Long sessionId);
    Optional<SessionExtension> findByRenewalId(Long renewalId);
    boolean existsByRenewalId(Long renewalId);
}
