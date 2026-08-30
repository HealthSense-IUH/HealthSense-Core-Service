package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.EpisodeHealthRecordAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EpisodeHealthRecordAuthorizationRepository
        extends JpaRepository<EpisodeHealthRecordAuthorization, Long> {

    Optional<EpisodeHealthRecordAuthorization> findBySessionIdAndHealthRecordId(Long sessionId, Long healthRecordId);

    boolean existsBySessionIdAndHealthRecordId(Long sessionId, Long healthRecordId);

    List<EpisodeHealthRecordAuthorization> findBySessionIdOrderByAuthorizedAtDesc(Long sessionId);

    List<EpisodeHealthRecordAuthorization> findBySessionIdIn(Collection<Long> sessionIds);
}
