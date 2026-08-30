package fit.iuh.se.hsoperations.repository;

import fit.iuh.se.hsoperations.entity.NotificationProjectionTask;
import fit.iuh.se.hsoperations.entity.enums.NotificationProjectionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface NotificationProjectionTaskRepository extends JpaRepository<NotificationProjectionTask, Long> {
    List<NotificationProjectionTask> findByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            Collection<NotificationProjectionStatus> statuses, Instant nextAttemptAt, Pageable pageable);
}
