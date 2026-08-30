package fit.iuh.se.hsoperations.service.impl;

import fit.iuh.se.hsoperations.event.NotificationProjectionRequested;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationProjectionListener {
    private final NotificationProjector projector;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(NotificationProjectionRequested event) {
        try {
            projector.project(event.taskId());
        } catch (RuntimeException ignored) {
            // The durable task remains retryable; notification failure never changes business success.
        }
    }
}
