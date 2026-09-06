package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationQueueEntry;
import fit.iuh.se.hschat.entity.enums.ConsultationQueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;

import java.util.Collection;
import java.util.Optional;

public interface ConsultationQueueEntryRepository extends JpaRepository<ConsultationQueueEntry, Long> {
    Optional<ConsultationQueueEntry> findByRequestId(Long requestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select entry from ConsultationQueueEntry entry where entry.requestId = :requestId")
    Optional<ConsultationQueueEntry> findByRequestIdForUpdate(Long requestId);

    boolean existsByMemberIdAndStatusIn(Long memberId, Collection<ConsultationQueueStatus> statuses);

    Optional<ConsultationQueueEntry> findFirstByMemberIdAndStatusInOrderByQueueDateAscQueueNumberAsc(
            Long memberId, Collection<ConsultationQueueStatus> statuses);

    @Query("""
            select count(entry) from ConsultationQueueEntry entry
            where entry.status in :statuses
              and (entry.queueDate < :queueDate
                   or (entry.queueDate = :queueDate and entry.queueNumber < :queueNumber))
            """)
    long countUnresolvedAhead(Collection<ConsultationQueueStatus> statuses, LocalDate queueDate, Long queueNumber);

    long countByStatusIn(Collection<ConsultationQueueStatus> statuses);

    Optional<ConsultationQueueEntry> findFirstByStatusOrderByQueueDateAscQueueNumberAsc(
            ConsultationQueueStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select entry from ConsultationQueueEntry entry where entry.id = :id")
    Optional<ConsultationQueueEntry> findByIdForUpdate(Long id);

    @Query(value = """
            select * from consultation_queue_entries
            where status = :#{#status.name()}
            order by queue_date asc, queue_number asc
            limit 1 for update
            """, nativeQuery = true)
    Optional<ConsultationQueueEntry> findFirstByStatusForUpdate(ConsultationQueueStatus status);

    java.util.List<ConsultationQueueEntry> findByStatusIn(Collection<ConsultationQueueStatus> statuses);
}
