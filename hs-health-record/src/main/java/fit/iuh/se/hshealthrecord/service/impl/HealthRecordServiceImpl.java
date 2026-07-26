package fit.iuh.se.hshealthrecord.service.impl;

import  fit.iuh.se.hshealthrecord.dto.request.AiCallbackRequest;
import fit.iuh.se.hshealthrecord.dto.request.PresignedUrlRequest;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.dto.response.PresignedUrlResponse;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import fit.iuh.se.hshealthrecord.mapper.HealthRecordMapper;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hshealthrecord.service.HealthRecordService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsshared.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthRecordServiceImpl implements HealthRecordService {

    private final HealthRecordRepository repository;
    private final HealthRecordMapper mapper;
    private final S3Service s3Service;

    @Override
    @Transactional
    public PresignedUrlResponse createPresignedUploadUrl(Long userId, PresignedUrlRequest request) {
        log.info("Creating presigned URL for user {} with file {}", userId, request.getFileName());
        
        String s3Key = s3Service.generateObjectKey(S3Service.FOLDER_RECORDS, userId, request.getFileName());
        String uploadUrl = s3Service.generatePresignedUploadUrl(s3Key, "text/csv");

        HealthRecord record = HealthRecord.builder()
                .userId(userId)
                .fileName(request.getFileName())
                .s3FileKey(s3Key)
                .fileSize(request.getFileSize())
                .status(RecordStatus.PENDING_UPLOAD)
                .build();

        record = repository.save(record);

        return PresignedUrlResponse.builder()
                .recordId(record.getId())
                .uploadUrl(uploadUrl)
                .s3Key(s3Key)
                .build();
    }

    @Override
    @Transactional
    public HealthRecordResponse confirmUpload(Long userId, Long recordId) {
        log.info("Confirming upload for record {} by user {}", recordId, userId);
        HealthRecord record = repository.findByIdAndUserId(recordId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.HEALTH_RECORD_NOT_FOUND));

        record.setStatus(RecordStatus.PROCESSING);
        record = repository.save(record);

        log.info("Record {} status updated to PROCESSING. Ready for AI Service processing.", recordId);
        // Ở giai đoạn sau (Phase 2), đây là nơi trigger Message Queue (RabbitMQ) hoặc gửi HTTP request sang AI Service
        
        return mapper.toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public HealthRecordResponse getRecordById(Long userId, Long recordId) {
        HealthRecord record = repository.findByIdAndUserId(recordId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.HEALTH_RECORD_NOT_FOUND));
        return mapper.toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HealthRecordResponse> getMyRecords(Long userId, Pageable pageable) {
        Page<HealthRecordResponse> page = repository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(mapper::toResponse);
        return new PageResponse<>(page);
    }

    @Override
    @Transactional
    public HealthRecordResponse updateAiResult(AiCallbackRequest request) {
        log.info("Updating AI result for record {}", request.getRecordId());
        HealthRecord record = repository.findById(request.getRecordId())
                .orElseThrow(() -> new AppException(ErrorCode.HEALTH_RECORD_NOT_FOUND));

        record.setPredictionLabel(request.getPredictionLabel());
        record.setConfidence(request.getConfidence());
        record.setHrvFeaturesJson(request.getHrvFeaturesJson());
        record.setStatus(RecordStatus.COMPLETED);

        record = repository.save(record);
        log.info("Record {} completed AI analysis with result: {}", record.getId(), record.getPredictionLabel());

        return mapper.toResponse(record);
    }
}
