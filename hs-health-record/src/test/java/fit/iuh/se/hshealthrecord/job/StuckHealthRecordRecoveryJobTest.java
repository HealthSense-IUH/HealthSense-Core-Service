package fit.iuh.se.hshealthrecord.job;

import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StuckHealthRecordRecoveryJobTest {

    private HealthRecordRepository repository;
    private StuckHealthRecordRecoveryJob job;

    @BeforeEach
    void setUp() {
        repository = mock(HealthRecordRepository.class);
        job = new StuckHealthRecordRecoveryJob(repository);
        ReflectionTestUtils.setField(job, "enabled", true);
        ReflectionTestUtils.setField(job, "timeoutMinutes", 10L);
    }

    @Test
    void marksTimedOutProcessingRecordsAsFailed() {
        HealthRecord stuck = HealthRecord.builder()
                .id(101L)
                .status(RecordStatus.PROCESSING)
                .build();
        when(repository.findTop50ByStatusAndUpdatedAtBeforeOrderByUpdatedAtAsc(eq(RecordStatus.PROCESSING), any(Instant.class)))
                .thenReturn(List.of(stuck));

        job.recoverStuckProcessingRecords();

        assertThat(stuck.getStatus()).isEqualTo(RecordStatus.FAILED);
        assertThat(stuck.getErrorMessage()).contains("timeout");
        verify(repository).saveAll(List.of(stuck));
    }

    @Test
    void doesNothingWhenDisabled() {
        ReflectionTestUtils.setField(job, "enabled", false);

        job.recoverStuckProcessingRecords();

        verify(repository, never()).findTop50ByStatusAndUpdatedAtBeforeOrderByUpdatedAtAsc(any(), any());
    }
}
