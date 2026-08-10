package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationFinalSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultationFinalSummaryRepository extends JpaRepository<ConsultationFinalSummary, Long> {

    Optional<ConsultationFinalSummary> findBySessionId(Long sessionId);

    boolean existsBySessionId(Long sessionId);
}
