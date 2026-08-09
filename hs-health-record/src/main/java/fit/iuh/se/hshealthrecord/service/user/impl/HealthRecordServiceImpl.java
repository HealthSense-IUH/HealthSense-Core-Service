package fit.iuh.se.hshealthrecord.service.user.impl;

import fit.iuh.se.hshealthrecord.dto.request.AiCallbackRequest;
import fit.iuh.se.hshealthrecord.dto.request.PresignedUrlRequest;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.dto.response.PresignedUrlResponse;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import fit.iuh.se.hshealthrecord.mapper.HealthRecordMapper;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hshealthrecord.dto.message.RecordProcessingMessage;
import fit.iuh.se.hshealthrecord.service.user.HealthRecordService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsshared.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthRecordServiceImpl implements HealthRecordService {

    private final HealthRecordRepository repository;
    private final HealthRecordMapper mapper;
    private final S3Service s3Service;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:health.record.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.processing:health.record.process.routing}")
    private String routingKey;

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

        log.info("Record {} status updated to PROCESSING. Triggering RabbitMQ event...", recordId);
        RecordProcessingMessage message = RecordProcessingMessage.builder()
                .recordId(record.getId())
                .s3Key(record.getS3FileKey())
                .userId(record.getUserId())
                .fileName(record.getFileName())
                .build();
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        log.info("Sent RecordProcessingMessage to exchange '{}' with routingKey '{}': {}", exchange, routingKey, message);
        
        return mapper.toResponse(record);
    }

    @Override
    @Transactional
    public HealthRecordResponse uploadDirectAndProcess(Long userId, MultipartFile file) {
        log.info("Direct upload and process for user {} with file {}", userId, file != null ? file.getOriginalFilename() : "null");
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File upload không được để trống");
        }
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "health_record.csv";
        String s3Key = s3Service.generateObjectKey(S3Service.FOLDER_RECORDS, userId, fileName);
        try {
            s3Service.uploadFileDirect(s3Key, file.getInputStream(), file.getSize(), file.getContentType() != null ? file.getContentType() : "text/csv");
        } catch (IOException e) {
            log.error("Error reading upload file: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi đọc file upload: " + e.getMessage(), e);
        }

        HealthRecord record = HealthRecord.builder()
                .userId(userId)
                .fileName(fileName)
                .s3FileKey(s3Key)
                .fileSize(file.getSize())
                .status(RecordStatus.PROCESSING)
                .build();
        record = repository.save(record);

        log.info("Record {} saved via direct upload. Triggering RabbitMQ event...", record.getId());
        RecordProcessingMessage message = RecordProcessingMessage.builder()
                .recordId(record.getId())
                .s3Key(record.getS3FileKey())
                .userId(record.getUserId())
                .fileName(record.getFileName())
                .build();
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        log.info("Sent RecordProcessingMessage to exchange '{}' with routingKey '{}': {}", exchange, routingKey, message);

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
    @Transactional(readOnly = true)
    public Optional<HealthRecordResponse> getLatestRecord(Long userId) {
        return repository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long countRecords(Long userId) {
        return repository.countByUserId(userId);
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
