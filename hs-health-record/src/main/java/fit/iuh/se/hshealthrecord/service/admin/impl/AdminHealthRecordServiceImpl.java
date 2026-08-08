package fit.iuh.se.hshealthrecord.service.admin.impl;

import fit.iuh.se.hshealthrecord.dto.request.AdminCreateHealthRecordRequest;
import fit.iuh.se.hshealthrecord.dto.request.AdminUpdateHealthRecordRequest;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import fit.iuh.se.hshealthrecord.mapper.HealthRecordMapper;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hshealthrecord.service.admin.AdminHealthRecordService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminHealthRecordServiceImpl implements AdminHealthRecordService {

    private static final String DEFAULT_HRV_FEATURES_JSON = "{\"HR_mean\":47.46528898934453,\"Mean_NN\":1264.0816326530612,\"SDNN\":440.4302743120779,\"RMSSD\":556.5874444625331,\"pNN50\":66.66666666666666,\"NN50\":32.0,\"CV\":34.84191708313177,\"LF\":0.029800441994053888,\"HF\":0.3003213839764191,\"LF_HF_Ratio\":0.09922850514165779,\"LF_norm\":9.027104435293932,\"HF_norm\":90.97289556470606,\"Total_Power\":0.330121825970473}";

    HealthRecordRepository repository;
    HealthRecordMapper mapper;
    UserAccountRepository userAccountRepository;

    @Override
    @Transactional
    public HealthRecordResponse createRecordForMember(Long adminId, AdminCreateHealthRecordRequest request) {
        if (adminId == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Admin ID must not be null");
        if (request == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Health record create request must not be null");

        UserAccount member = userAccountRepository.findById(request.getMemberId())
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        if (member.getRole() != UserRole.MEMBER)
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);

        String fileName = defaultIfBlank(request.getFileName(), "sample-hrv-consultation.csv");
        HealthRecord record = HealthRecord.builder()
                .userId(request.getMemberId())
                .fileName(fileName)
                .s3FileKey(defaultIfBlank(
                        request.getS3FileKey(),
                        "seed/health-records/" + request.getMemberId() + "/" + fileName
                ))
                .fileSize(request.getFileSize() == null ? 2048L : request.getFileSize())
                .status(request.getStatus() == null ? RecordStatus.COMPLETED : request.getStatus())
                .predictionLabel(request.getPredictionLabel() == null ? PredictionLabel.UNCERTAIN : request.getPredictionLabel())
                .confidence(request.getConfidence() == null ? 0.78 : request.getConfidence())
                .hrvFeaturesJson(defaultIfBlank(
                        request.getHrvFeaturesJson(),
                        DEFAULT_HRV_FEATURES_JSON
                ))
                .build();

        return mapper.toResponse(repository.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HealthRecordResponse> getRecords(
            Long memberId,
            RecordStatus status,
            PredictionLabel predictionLabel,
            String keyword,
            Instant fromDate,
            Instant toDate,
            Pageable pageable) {
        if (pageable == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Pageable must not be null");
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate))
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "From date must be before to date");

        Page<HealthRecordResponse> page = repository.findAll(
                buildFilter(memberId, status, predictionLabel, trimToNull(keyword), fromDate, toDate),
                pageable
        ).map(mapper::toResponse);
        return new PageResponse<>(page);
    }

    @Override
    @Transactional(readOnly = true)
    public HealthRecordResponse getRecord(Long id) {
        return mapper.toResponse(findRecord(id));
    }

    private HealthRecord findRecord(Long id) {
        if (id == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Health record ID must not be null");
        return repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HEALTH_RECORD_NOT_FOUND));
    }

    private Specification<HealthRecord> buildFilter(
            Long memberId,
            RecordStatus status,
            PredictionLabel predictionLabel,
            String keyword,
            Instant fromDate,
            Instant toDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (memberId != null)
                predicates.add(criteriaBuilder.equal(root.get("userId"), memberId));
            if (status != null)
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            if (predictionLabel != null)
                predicates.add(criteriaBuilder.equal(root.get("predictionLabel"), predictionLabel));
            if (fromDate != null)
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            if (toDate != null)
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate));
            if (keyword != null) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("fileName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("s3FileKey")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("hrvFeaturesJson")), pattern)
                ));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String defaultIfBlank(String value, String defaultValue) {
        String normalized = trimToNull(value);
        return normalized == null ? defaultValue : normalized;
    }

    private String requireText(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, message);
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
