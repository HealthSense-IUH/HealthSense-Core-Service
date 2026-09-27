package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationFinalSummary;
import fit.iuh.se.hschat.entity.enums.ConsultationFinalSummaryStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationFinalSummaryRepository extends JpaRepository<ConsultationFinalSummary, Long> {

    Optional<ConsultationFinalSummary> findBySessionId(Long sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select summary from ConsultationFinalSummary summary where summary.sessionId = :sessionId")
    Optional<ConsultationFinalSummary> findBySessionIdForUpdate(Long sessionId);

    boolean existsBySessionId(Long sessionId);

    List<ConsultationFinalSummary> findBySessionIdInAndStatus(
            Collection<Long> sessionIds, ConsultationFinalSummaryStatus status);
}
