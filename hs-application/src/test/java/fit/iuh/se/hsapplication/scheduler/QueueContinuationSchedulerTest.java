package fit.iuh.se.hsapplication.scheduler;

import fit.iuh.se.hschat.service.continuation.QueueContinuationService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QueueContinuationSchedulerTest {

    @Test
    void boundedScanProcessesEachDueSessionAndIsolatesOneFailure() {
        QueueContinuationService service = mock(QueueContinuationService.class);
        QueueContinuationScheduler scheduler = new QueueContinuationScheduler(service);
        scheduler.batchSize = 2;
        when(service.findDueSessionIds(any(Instant.class), eq(2))).thenReturn(List.of(1L, 2L));
        doThrow(new RuntimeException("one session failed"))
                .when(service).processDeadline(eq(1L), any(Instant.class));

        scheduler.processDeadlines();

        verify(service).processDeadline(eq(1L), any(Instant.class));
        verify(service).processDeadline(eq(2L), any(Instant.class));
    }
}
