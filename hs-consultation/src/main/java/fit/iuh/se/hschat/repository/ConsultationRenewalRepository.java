package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationRenewal;
import fit.iuh.se.hschat.entity.enums.ConsultationRenewalStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConsultationRenewalRepository extends JpaRepository<ConsultationRenewal, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select renewal from ConsultationRenewal renewal where renewal.id = :id")
    Optional<ConsultationRenewal> findByIdForUpdate(Long id);

    Optional<ConsultationRenewal> findByIdAndMemberId(Long id, Long memberId);

    List<ConsultationRenewal> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    boolean existsBySessionIdAndStatusIn(Long sessionId, Collection<ConsultationRenewalStatus> statuses);

    long countByDoctorIdAndSessionIdNotAndStatusInAndPreviousEndsAtLessThanAndProposedNewEndsAtGreaterThan(
            Long doctorId,
            Long sessionId,
            Collection<ConsultationRenewalStatus> statuses,
            Instant windowEnd,
            Instant windowStart);

    List<ConsultationRenewal> findByStatusInAndPaymentDeadlineBefore(
            Collection<ConsultationRenewalStatus> statuses, Instant deadline);
}
