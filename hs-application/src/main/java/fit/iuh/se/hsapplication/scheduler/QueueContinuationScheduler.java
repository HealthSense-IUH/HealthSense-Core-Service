package fit.iuh.se.hsapplication.scheduler;

import fit.iuh.se.hschat.service.continuation.QueueContinuationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueueContinuationScheduler {

    private final QueueContinuationService continuationService;

    @Value("${app.consultation.continuation.scheduler-batch-size:100}")
    int batchSize;

    @Scheduled(fixedDelayString = "${app.consultation.continuation.scheduler-delay-ms:1000}")
    public void processDeadlines() {
        Instant now = Instant.now();
        try {
            continuationService.findDueSessionIds(now, batchSize)
                    .forEach(sessionId -> processOne(sessionId, now));
        } catch (RuntimeException exception) {
            log.warn("Queue continuation deadline scan failed; a later tick will retry", exception);
        }
    }

    private void processOne(Long sessionId, Instant now) {
        try {
            continuationService.processDeadline(sessionId, now);
        } catch (RuntimeException exception) {
            log.warn("Queue continuation processing failed for session {}; a later tick will retry",
                    sessionId, exception);
        }
    }
}
