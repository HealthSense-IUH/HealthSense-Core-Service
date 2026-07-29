package fit.iuh.se.hshealthrecord.service;

import fit.iuh.se.hshealthrecord.dto.request.AiCallbackRequest;
import fit.iuh.se.hshealthrecord.dto.request.PresignedUrlRequest;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.dto.response.PresignedUrlResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface HealthRecordService {

    PresignedUrlResponse createPresignedUploadUrl(Long userId, PresignedUrlRequest request);

    HealthRecordResponse confirmUpload(Long userId, Long recordId);

    HealthRecordResponse uploadDirectAndProcess(Long userId, MultipartFile file);

    HealthRecordResponse getRecordById(Long userId, Long recordId);

    PageResponse<HealthRecordResponse> getMyRecords(Long userId, Pageable pageable);

    HealthRecordResponse updateAiResult(AiCallbackRequest request);
}
