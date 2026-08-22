package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationSessionRepository extends JpaRepository<ConsultationSession, Long> {

    Optional<ConsultationSession> findByRequestId(Long requestId);

    boolean existsByRequestId(Long requestId);

    Optional<ConsultationSession> findByMemberIdAndStatus(Long memberId, ConsultationStatus status);

    boolean existsByMemberIdAndStatus(Long memberId, ConsultationStatus status);

    boolean existsByMemberIdAndStatusIn(Long memberId, Collection<ConsultationStatus> statuses);

    long countByDoctorIdAndStatus(Long doctorId, ConsultationStatus status);

    long countByDoctorIdAndStatusIn(Long doctorId, Collection<ConsultationStatus> statuses);

    Page<ConsultationSession> findByMemberIdOrderByLastMessageAtDesc(Long memberId, Pageable pageable);

    Page<ConsultationSession> findByDoctorIdOrderByLastMessageAtDesc(Long doctorId, Pageable pageable);

    Page<ConsultationSession> findByDoctorIdAndStatusInOrderByLastMessageAtDesc(Long doctorId, Collection<ConsultationStatus> statuses, Pageable pageable);

    Optional<ConsultationSession> findByIdAndDoctorId(Long id, Long doctorId);

    Page<ConsultationSession> findByStatusOrderByCreatedAtDesc(ConsultationStatus status, Pageable pageable);

    List<ConsultationSession> findByStatusAndEndsAtBefore(ConsultationStatus status, Instant endsAt);

    List<ConsultationSession> findByStatusAndStartedAtBefore(ConsultationStatus status, Instant startedAt);

    List<ConsultationSession> findAllByMemberIdAndStatus(Long memberId, ConsultationStatus status);
}
