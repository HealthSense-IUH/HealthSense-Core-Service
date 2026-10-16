package fit.iuh.se.hschat.service.statistics.impl;

import fit.iuh.se.hschat.dto.response.statistics.DoctorConsultationStatisticsResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientConsultationResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientDetailResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientHealthRecordResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientListItemResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientMedicalHistoryResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientStatisticsResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorStatisticsPeriodResponse;
import fit.iuh.se.hschat.repository.statistics.DoctorStatisticsQueryRepository;
import fit.iuh.se.hschat.service.statistics.DoctorStatisticsService;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.entity.enums.BusinessActorType;
import fit.iuh.se.hsoperations.entity.enums.BusinessDomainType;
import fit.iuh.se.hsoperations.entity.enums.BusinessEventType;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DoctorStatisticsServiceImpl implements DoctorStatisticsService {

    static final long MAX_RANGE_DAYS = 366;

    private final DoctorStatisticsQueryRepository repository;
    private final OperationalEventPublisher operationalEvents;

    @Override
    @Transactional(readOnly = true)
    public DoctorConsultationStatisticsResponse getConsultationStatistics(
            Long doctorId, Instant from, Instant to) {
        validateActor(doctorId);
        validateRange(from, to);
        var value = repository.summarizeConsultations(doctorId, from, to);
        return new DoctorConsultationStatisticsResponse(
                new DoctorStatisticsPeriodResponse(from, to),
                new DoctorConsultationStatisticsResponse.Summary(
                        value.totalConsultations(),
                        value.consultationsInPeriod(),
                        value.scheduledInPeriod(),
                        value.activeInPeriod(),
                        value.completedInPeriod(),
                        value.cancelledInPeriod(),
                        percentage(value.completedInPeriod(), value.consultationsInPeriod()),
                        percentage(value.cancelledInPeriod(), value.consultationsInPeriod())));
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorPatientStatisticsResponse getPatientStatistics(Long doctorId, Instant from, Instant to) {
        validateActor(doctorId);
        validateRange(from, to);
        var value = repository.summarizePatients(doctorId, from, to);
        return new DoctorPatientStatisticsResponse(
                new DoctorStatisticsPeriodResponse(from, to),
                new DoctorPatientStatisticsResponse.Summary(
                        value.totalPatients(),
                        value.patientsInPeriod(),
                        value.newPatientsInPeriod(),
                        value.returningPatientsInPeriod(),
                        average(value.consultationsInPeriod(), value.patientsInPeriod())));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorPatientListItemResponse> getPatients(
            Long doctorId, Instant from, Instant to, String keyword, Pageable pageable) {
        validateActor(doctorId);
        validateRange(from, to);
        return new PageResponse<>(repository.findPatients(
                doctorId, from, to, keyword == null ? "" : keyword.trim(), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorPatientDetailResponse getPatient(Long doctorId, Long memberId) {
        validateActor(doctorId);
        validateMember(memberId);
        return repository.findPatientDetail(doctorId, memberId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorPatientConsultationResponse> getPatientConsultations(
            Long doctorId, Long memberId, Pageable pageable) {
        requireRelationship(doctorId, memberId);
        return new PageResponse<>(repository.findConsultationHistory(doctorId, memberId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorPatientMedicalHistoryResponse> getPatientMedicalHistory(
            Long doctorId, Long memberId, Pageable pageable) {
        requireRelationship(doctorId, memberId);
        return new PageResponse<>(repository.findMedicalHistory(doctorId, memberId, pageable));
    }

    @Override
    @Transactional
    public PageResponse<DoctorPatientHealthRecordResponse> getPatientHealthRecords(
            Long doctorId, Long memberId, Pageable pageable) {
        requireRelationship(doctorId, memberId);
        var page = repository.findAuthorizedHealthRecords(doctorId, memberId, pageable);
        page.getContent().forEach(record -> auditHealthRecordAccess(doctorId, memberId, record));
        return new PageResponse<>(page);
    }

    private void requireRelationship(Long doctorId, Long memberId) {
        validateActor(doctorId);
        validateMember(memberId);
        if (!repository.hasPatientRelationship(doctorId, memberId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
    }

    private void validateActor(Long doctorId) {
        if (doctorId == null || doctorId <= 0) throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private void validateMember(Long memberId) {
        if (memberId == null || memberId <= 0)
            throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.member-id-invalid");
    }

    private void validateRange(Instant from, Instant to) {
        if (from == null) throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.statistics-from-required");
        if (to == null) throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.statistics-to-required");
        if (!from.isBefore(to))
            throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.from-date-before-to-date");
        if (Duration.between(from, to).compareTo(Duration.ofDays(MAX_RANGE_DAYS)) > 0)
            throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.statistics-range-max-days", MAX_RANGE_DAYS);
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator == 0) return BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal average(long numerator, long denominator) {
        if (denominator == 0) return BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private void auditHealthRecordAccess(
            Long doctorId, Long memberId, DoctorPatientHealthRecordResponse record) {
        Instant bucket = Instant.now().truncatedTo(ChronoUnit.HOURS);
        operationalEvents.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.HEALTH_RECORD)
                .domainId(record.recordId())
                .eventType(BusinessEventType.HEALTH_RECORD_HISTORICAL_ACCESSED)
                .actorType(BusinessActorType.USER)
                .actorUserId(doctorId)
                .actorRole(UserRole.DOCTOR.name())
                .sessionId(record.sessionId())
                .healthRecordId(record.recordId())
                .memberId(memberId)
                .doctorId(doctorId)
                .metadata(Map.of("accessContext", "DOCTOR_PATIENT_HISTORY"))
                .occurredAt(Instant.now())
                .idempotencyKey("doctor-patient-health-history:" + doctorId + ":" + memberId
                        + ":" + record.recordId() + ":" + bucket)
                .build());
    }
}
