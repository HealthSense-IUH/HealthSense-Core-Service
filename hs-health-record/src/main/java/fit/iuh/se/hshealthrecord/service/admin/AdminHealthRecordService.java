package fit.iuh.se.hshealthrecord.service.admin;

import fit.iuh.se.hshealthrecord.dto.request.AdminCreateHealthRecordRequest;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface AdminHealthRecordService {

    HealthRecordResponse createRecordForMember(Long adminId, AdminCreateHealthRecordRequest request);

    PageResponse<HealthRecordResponse> getRecords(
            Long memberId,
            RecordStatus status,
            PredictionLabel predictionLabel,
            String keyword,
            Instant fromDate,
            Instant toDate,
            Pageable pageable
    );

    HealthRecordResponse getRecord(Long id);

    Object getSystemStatistics(Instant fromDate, Instant toDate);
}
