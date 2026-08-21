package fit.iuh.se.hshealthrecord.service.user.impl;

import fit.iuh.se.hshealthrecord.dto.request.AiCallbackRequest;
import fit.iuh.se.hshealthrecord.dto.request.PresignedUrlRequest;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.dto.response.PresignedUrlResponse;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import fit.iuh.se.hshealthrecord.event.HealthRecordAnalyzedEvent;
import fit.iuh.se.hshealthrecord.mapper.HealthRecordMapper;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hshealthrecord.dto.message.RecordProcessingMessage;
import fit.iuh.se.hshealthrecord.service.user.HealthRecordService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import fit.iuh.se.hshealthrecord.dto.response.HealthStatItemResponse;
import fit.iuh.se.hshealthrecord.dto.response.HealthStatisticsResponse;
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthRecordServiceImpl implements HealthRecordService {

    private final HealthRecordRepository repository;
    private final HealthRecordMapper mapper;
    private final S3Service s3Service;
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher eventPublisher;

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
        eventPublisher.publishEvent(HealthRecordAnalyzedEvent.builder()
                .recordId(record.getId())
                .userId(record.getUserId())
                .predictionLabel(record.getPredictionLabel())
                .confidence(record.getConfidence())
                .analyzedAt(record.getUpdatedAt())
                .build());

        return mapper.toResponse(record);
    }

    @Override
    @Transactional
    public HealthRecordResponse markAsFailed(fit.iuh.se.hshealthrecord.dto.request.AiCallbackFailedRequest request) {
        log.info("Marking AI processing as failed for record {}", request.getRecordId());
        HealthRecord record = repository.findById(request.getRecordId())
                .orElseThrow(() -> new AppException(ErrorCode.HEALTH_RECORD_NOT_FOUND));

        record.setStatus(RecordStatus.FAILED);
        record.setErrorMessage(request.getErrorReason());

        record = repository.save(record);
        log.info("Record {} failed AI analysis with reason: {}", record.getId(), record.getErrorMessage());

        return mapper.toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public HealthStatisticsResponse getHealthStatistics(Long userId, String period, String referenceDate, String timezone) {
        ZoneId zoneId = (timezone != null && !timezone.isEmpty()) ? ZoneId.of(timezone) : ZoneId.of("UTC");
        
        // Parse reference date, default to now if not provided
        ZonedDateTime refZoned;
        try {
            if (referenceDate != null && !referenceDate.isEmpty()) {
                if (referenceDate.contains("T")) {
                    refZoned = ZonedDateTime.parse(referenceDate).withZoneSameInstant(zoneId);
                } else {
                    refZoned = LocalDate.parse(referenceDate).atStartOfDay(zoneId);
                }
            } else {
                refZoned = ZonedDateTime.now(zoneId);
            }
        } catch (Exception e) {
            log.warn("Failed to parse referenceDate: {}, using current time", referenceDate);
            refZoned = ZonedDateTime.now(zoneId);
        }

        ZonedDateTime startZoned;
        ZonedDateTime endZoned;
        int numOfItems;
        
        // Determine time boundaries
        switch (period.toUpperCase()) {
            case "DAY":
                startZoned = refZoned.truncatedTo(ChronoUnit.DAYS);
                endZoned = startZoned.plusDays(1).minusNanos(1);
                numOfItems = 24;
                break;
            case "WEEK":
                startZoned = refZoned.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).truncatedTo(ChronoUnit.DAYS);
                endZoned = startZoned.plusDays(7).minusNanos(1);
                numOfItems = 7;
                break;
            case "MONTH":
                startZoned = refZoned.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
                endZoned = refZoned.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).truncatedTo(ChronoUnit.DAYS).minusNanos(1);
                numOfItems = startZoned.toLocalDate().lengthOfMonth();
                break;
            case "YEAR":
            default:
                startZoned = refZoned.with(TemporalAdjusters.firstDayOfYear()).truncatedTo(ChronoUnit.DAYS);
                endZoned = refZoned.with(TemporalAdjusters.lastDayOfYear()).plusDays(1).truncatedTo(ChronoUnit.DAYS).minusNanos(1);
                numOfItems = 12;
                break;
        }

        Instant from = startZoned.toInstant();
        Instant to = endZoned.toInstant();
        
        List<HealthStatItemResponse> chartData = new ArrayList<>();
        
        // Initialize chart data with empty values
        for (int i = 1; i <= numOfItems; i++) {
            String label = String.valueOf(i);
            if (period.equalsIgnoreCase("DAY")) {
                label = (i - 1) + "h"; // 0h to 23h
            } else if (period.equalsIgnoreCase("WEEK")) {
                String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
                label = days[i - 1];
            } else if (period.equalsIgnoreCase("YEAR")) {
                label = "T" + i; // T1 to T12
            }
            
            chartData.add(HealthStatItemResponse.builder()
                    .label(label)
                    .normalCount(0)
                    .afibRiskCount(0)
                    .uncertainCount(0)
                    .afibSuspectedCount(0)
                    .build());
        }

        // Fetch aggregated data from PostgreSQL Native Queries
        List<fit.iuh.se.hshealthrecord.repository.HealthStatProjection> aggregatedStats;
        String pgTimezone = zoneId.getId(); // E.g., "Asia/Ho_Chi_Minh"

        aggregatedStats = switch (period.toUpperCase()) {
            case "DAY" -> repository.getStatsByDay(userId, from, to, pgTimezone);
            case "WEEK" -> repository.getStatsByWeek(userId, from, to, pgTimezone);
            case "MONTH" -> repository.getStatsByMonth(userId, from, to, pgTimezone);
            default -> repository.getStatsByYear(userId, from, to, pgTimezone);
        };

        int totalNormal = 0;
        int totalAfibRisk = 0;
        int totalUncertain = 0;
        int totalAfibSuspected = 0;

        // Map projection data to chartData
        for (fit.iuh.se.hshealthrecord.repository.HealthStatProjection stat : aggregatedStats) {
            if (stat.getStatGroup() == null) continue;
            
            int statGroup = stat.getStatGroup().intValue();
            int normalCount = stat.getNormalCount() != null ? stat.getNormalCount() : 0;
            int afibRiskCount = stat.getAfibRiskCount() != null ? stat.getAfibRiskCount() : 0;
            int uncertainCount = stat.getUncertainCount() != null ? stat.getUncertainCount() : 0;
            int afibSuspectedCount = stat.getAfibSuspectedCount() != null ? stat.getAfibSuspectedCount() : 0;
            
            int index = switch (period.toUpperCase()) {
                case "DAY" -> statGroup; // HOUR (0-23)
                case "WEEK" -> statGroup - 1; // ISODOW (1-7) -> (0-6)
                case "MONTH" -> statGroup - 1; // DAY (1-31) -> (0-30)
                default -> statGroup - 1; // MONTH (1-12) -> (0-11)
            }; // 0-based index in chartData list

            if (index >= 0 && index < chartData.size()) {
                HealthStatItemResponse item = chartData.get(index);
                item.setNormalCount(normalCount);
                item.setAfibRiskCount(afibRiskCount);
                item.setUncertainCount(uncertainCount);
                item.setAfibSuspectedCount(afibSuspectedCount);
                
                totalNormal += normalCount;
                totalAfibRisk += afibRiskCount;
                totalUncertain += uncertainCount;
                totalAfibSuspected += afibSuspectedCount;
            }
        }

        return HealthStatisticsResponse.builder()
                .chartData(chartData)
                .totalNormal(totalNormal)
                .totalAfibRisk(totalAfibRisk)
                .totalUncertain(totalUncertain)
                .totalAfibSuspected(totalAfibSuspected)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableHistoryDates(Long userId, String timezone) {
        String pgTimezone = (timezone != null && !timezone.isEmpty()) ? timezone : "UTC";
        List<java.sql.Date> dates = repository.findDistinctDatesByUserId(userId, pgTimezone);
        
        List<String> result = new ArrayList<>();
        for (java.sql.Date d : dates) {
            if (d != null) {
                result.add(d.toString());
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthRecordResponse> getRecordsByDate(Long userId, String date, String timezone) {
        ZoneId zoneId = (timezone != null && !timezone.isEmpty()) ? ZoneId.of(timezone) : ZoneId.of("UTC");
        
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Expected YYYY-MM-DD");
        }
        
        Instant startOfDay = localDate.atStartOfDay(zoneId).toInstant();
        Instant endOfDay = localDate.plusDays(1).atStartOfDay(zoneId).minusNanos(1).toInstant();
        
        List<HealthRecord> records = repository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startOfDay, endOfDay);
        
        List<HealthRecordResponse> responses = new ArrayList<>();
        for (HealthRecord record : records) {
            responses.add(mapper.toResponse(record));
        }
        return responses;
    }
}
