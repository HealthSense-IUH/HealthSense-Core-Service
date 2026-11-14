package fit.iuh.se.hshealthrecord.job;

import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StuckHealthRecordRecoveryJob {

    private final HealthRecordRepository repository;

    @Value("${app.health.stuck-recovery.enabled:true}")
    private boolean enabled;

    @Value("${app.health.stuck-recovery.timeout-minutes:10}")
    private long timeoutMinutes;

    @Scheduled(fixedDelayString = "${app.health.stuck-recovery.interval-ms:300000}")
    @Transactional
    public void recoverStuckProcessingRecords() {
        if (!enabled) {
            return;
        }
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(Math.max(1, timeoutMinutes)));
        List<HealthRecord> stuckRecords = repository.findTop50ByStatusAndUpdatedAtBeforeOrderByUpdatedAtAsc(
                RecordStatus.PROCESSING, cutoff);
        if (stuckRecords.isEmpty()) {
            return;
        }
        for (HealthRecord record : stuckRecords) {
            record.setStatus(RecordStatus.FAILED);
            record.setErrorMessage("Quá thời gian xử lý phân tích tín hiệu (timeout)");
        }
        repository.saveAll(stuckRecords);
        log.warn("[StuckRecordRecovery] Đã chuyển {} bản ghi kẹt PROCESSING quá {} phút sang FAILED.",
                stuckRecords.size(), timeoutMinutes);
    }
}
