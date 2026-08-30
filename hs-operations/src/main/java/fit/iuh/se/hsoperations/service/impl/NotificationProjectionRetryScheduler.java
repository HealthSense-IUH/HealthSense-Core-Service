package fit.iuh.se.hsoperations.service.impl;

import fit.iuh.se.hsoperations.entity.enums.NotificationProjectionStatus;
import fit.iuh.se.hsoperations.repository.NotificationProjectionTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationProjectionRetryScheduler {
    private final NotificationProjectionTaskRepository repository;
    private final NotificationProjector projector;

    @Scheduled(fixedDelayString = "${operations.notification.retry-delay-ms:60000}")
    public void retry() {
        repository.findByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                List.of(NotificationProjectionStatus.PENDING, NotificationProjectionStatus.FAILED),
                Instant.now(), PageRequest.of(0, 50)).forEach(task -> projector.project(task.getId()));
    }
}
