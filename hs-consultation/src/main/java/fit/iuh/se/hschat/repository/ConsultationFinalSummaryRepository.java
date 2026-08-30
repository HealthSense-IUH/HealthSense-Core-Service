package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationFinalSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import fit.iuh.se.hschat.entity.enums.ConsultationFinalSummaryStatus;

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
