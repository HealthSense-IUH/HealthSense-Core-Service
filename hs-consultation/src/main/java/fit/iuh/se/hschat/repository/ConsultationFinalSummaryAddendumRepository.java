package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationFinalSummaryAddendum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultationFinalSummaryAddendumRepository
        extends JpaRepository<ConsultationFinalSummaryAddendum, Long> {

    List<ConsultationFinalSummaryAddendum> findBySummaryIdOrderByAuthoredAtAsc(Long summaryId);
}
