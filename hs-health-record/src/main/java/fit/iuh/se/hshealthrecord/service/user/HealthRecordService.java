package fit.iuh.se.hshealthrecord.service.user;

import fit.iuh.se.hshealthrecord.dto.request.AiCallbackRequest;
import fit.iuh.se.hshealthrecord.dto.request.PresignedUrlRequest;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.dto.response.PresignedUrlResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface HealthRecordService {

    PresignedUrlResponse createPresignedUploadUrl(Long userId, PresignedUrlRequest request);

    HealthRecordResponse confirmUpload(Long userId, Long recordId);

    HealthRecordResponse uploadDirectAndProcess(Long userId, MultipartFile file);

    HealthRecordResponse getRecordById(Long userId, Long recordId);

    PageResponse<HealthRecordResponse> getMyRecords(Long userId, Pageable pageable);

    Optional<HealthRecordResponse> getLatestRecord(Long userId);

    long countRecords(Long userId);

    HealthRecordResponse updateAiResult(AiCallbackRequest request);

    fit.iuh.se.hshealthrecord.dto.response.HealthStatisticsResponse getHealthStatistics(Long userId, String period, String referenceDate, String timezone);
}
