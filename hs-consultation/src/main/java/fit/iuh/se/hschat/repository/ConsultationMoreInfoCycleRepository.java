package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationMoreInfoCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationMoreInfoCycleRepository extends JpaRepository<ConsultationMoreInfoCycle, Long> {

    List<ConsultationMoreInfoCycle> findByRequestIdOrderByRequestedAtAsc(Long requestId);

    Optional<ConsultationMoreInfoCycle> findFirstByRequestIdAndRespondedAtIsNullOrderByRequestedAtDesc(Long requestId);
}
