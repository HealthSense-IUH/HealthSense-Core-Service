package fit.iuh.se.hsshared.service.s3.event;

import fit.iuh.se.hsshared.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3FileEventListener {

    private final S3Service s3Service;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleS3FileMoveEvent(S3FileMoveEvent event) {
        log.info("Received S3FileMoveEvent: moving {} to {}", event.getSourceKey(), event.getDestinationKey());
        try {
            s3Service.moveFile(event.getSourceKey(), event.getDestinationKey());
        } catch (Exception e) {
            log.error("Error handling S3FileMoveEvent: {}", e.getMessage(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleS3FileDeleteEvent(S3FileDeleteEvent event) {
        log.info("Received S3FileDeleteEvent: deleting {}", event.getObjectKey());
        try {
            s3Service.deleteFile(event.getObjectKey());
        } catch (Exception e) {
            log.error("Error handling S3FileDeleteEvent: {}", e.getMessage(), e);
        }
    }
}
