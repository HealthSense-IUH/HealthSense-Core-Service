package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.entity.enums.FinalSummaryClosureStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationFlowType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationSessionRepository extends JpaRepository<ConsultationSession, Long> {

    Optional<ConsultationSession> findByRequestId(Long requestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from ConsultationSession session where session.id = :id")
    Optional<ConsultationSession> findByIdForUpdate(Long id);

    boolean existsByRequestId(Long requestId);

    Optional<ConsultationSession> findByMemberIdAndStatus(Long memberId, ConsultationStatus status);

    Optional<ConsultationSession> findByMemberIdAndFlowTypeAndStatus(
            Long memberId, ConsultationFlowType flowType, ConsultationStatus status);

    boolean existsByMemberIdAndFlowTypeAndStatus(
            Long memberId, ConsultationFlowType flowType, ConsultationStatus status);

    boolean existsByMemberIdAndStatus(Long memberId, ConsultationStatus status);

    boolean existsByMemberIdAndStatusIn(Long memberId, Collection<ConsultationStatus> statuses);

    boolean existsByDoctorIdAndStatusIn(Long doctorId, Collection<ConsultationStatus> statuses);

    @Query("select distinct session.doctorId from ConsultationSession session where session.status in :statuses")
    List<Long> findDistinctDoctorIdsByStatusIn(Collection<ConsultationStatus> statuses);

    long countByDoctorIdAndStatus(Long doctorId, ConsultationStatus status);

    long countByDoctorIdAndStatusIn(Long doctorId, Collection<ConsultationStatus> statuses);

    long countByDoctorIdAndIdNotAndStatusInAndStartedAtLessThanAndEndsAtGreaterThan(
            Long doctorId,
            Long excludedSessionId,
            Collection<ConsultationStatus> statuses,
            Instant windowEnd,
            Instant windowStart);

    Page<ConsultationSession> findByMemberIdOrderByLastMessageAtDesc(Long memberId, Pageable pageable);

    Page<ConsultationSession> findByMemberIdOrderByStartedAtDesc(Long memberId, Pageable pageable);

    Page<ConsultationSession> findByMemberIdAndActivatedAtIsNotNullOrderByStartedAtDesc(
            Long memberId, Pageable pageable);

    List<ConsultationSession> findByMemberIdAndIdNotAndActivatedAtIsNotNullOrderByStartedAtDesc(
            Long memberId, Long id);

    Page<ConsultationSession> findByDoctorIdOrderByLastMessageAtDesc(Long doctorId, Pageable pageable);

    Page<ConsultationSession> findByDoctorIdAndStatusInOrderByLastMessageAtDesc(Long doctorId, Collection<ConsultationStatus> statuses, Pageable pageable);

    Optional<ConsultationSession> findByIdAndDoctorId(Long id, Long doctorId);

    Page<ConsultationSession> findByStatusOrderByCreatedAtDesc(ConsultationStatus status, Pageable pageable);

    List<ConsultationSession> findByFlowTypeAndStatusAndEndsAtBefore(
            ConsultationFlowType flowType, ConsultationStatus status, Instant endsAt);

    List<ConsultationSession> findByFlowTypeAndStatusAndEndsAtLessThanEqualOrderByEndsAtAsc(
            ConsultationFlowType flowType, ConsultationStatus status, Instant endsAt, Pageable pageable);

    List<ConsultationSession> findByFlowTypeAndStatusAndEndsAtBetween(
            ConsultationFlowType flowType, ConsultationStatus status, Instant startsAt, Instant endsAt);

    List<ConsultationSession> findByStatusAndStartedAtBefore(ConsultationStatus status, Instant startedAt);

    List<ConsultationSession> findAllByMemberIdAndStatus(Long memberId, ConsultationStatus status);

    List<ConsultationSession> findBySummaryClosureStatusIn(
            Collection<FinalSummaryClosureStatus> statuses);

    @Query("""
            select session.id from ConsultationSession session
            where session.flowType = :flowType
              and session.status = :status
              and session.doctorReleasedAt is null
              and (
                (session.summaryClosureStatus = :pendingStatus
                    and (session.summaryDueAt is null
                         or session.summaryDueAt <= :now
                         or session.completedAt <= :completedBefore))
                or session.summaryClosureStatus = :finalizedStatus
              )
            order by session.summaryDueAt asc, session.id asc
            """)
    List<Long> findQueueSummaryReleaseCandidateIds(
            ConsultationFlowType flowType,
            ConsultationStatus status,
            FinalSummaryClosureStatus pendingStatus,
            FinalSummaryClosureStatus finalizedStatus,
            Instant now,
            Instant completedBefore,
            Pageable pageable);
}
