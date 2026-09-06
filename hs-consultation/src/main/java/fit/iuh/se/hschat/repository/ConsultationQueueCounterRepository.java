package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationQueueCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface ConsultationQueueCounterRepository extends JpaRepository<ConsultationQueueCounter, LocalDate> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select counter from ConsultationQueueCounter counter where counter.queueDate = :queueDate")
    Optional<ConsultationQueueCounter> findByQueueDateForUpdate(LocalDate queueDate);
}
