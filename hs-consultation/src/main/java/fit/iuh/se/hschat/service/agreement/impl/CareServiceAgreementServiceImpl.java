package fit.iuh.se.hschat.service.agreement.impl;

import fit.iuh.se.hschat.dto.response.CareServiceAgreementResponse;
import fit.iuh.se.hschat.entity.*;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.*;
import fit.iuh.se.hschat.service.agreement.CareServiceAgreementService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CareServiceAgreementServiceImpl implements CareServiceAgreementService {

    static final List<CareServiceAgreementStatus> INVALIDATABLE_STATUSES = List.of(
            CareServiceAgreementStatus.DRAFT,
            CareServiceAgreementStatus.PENDING_ACCEPTANCE,
            CareServiceAgreementStatus.ACCEPTED
    );
    static final String DEFAULT_TERMS = "HEALTHSENSE_TERMS_V1";
    static final String CANCELLATION_POLICY = "HEALTHSENSE_CANCELLATION_POLICY_V1";
    static final String REFUND_POLICY = "HEALTHSENSE_REFUND_POLICY_V1";
    static final String EMERGENCY_LIMITATION =
            "This service is not an emergency service and does not guarantee immediate response.";
    static final String AI_LIMITATION =
            "AI outputs are decision support only and do not replace professional clinical judgment.";
    static final String SERVICE_LIMITATION =
            "Care is limited to the agreed package scope and the assigned Doctor support schedule.";
    static final String HEALTH_DATA_SCOPE =
            "Only health data explicitly selected or shared for this care episode is authorized for care use.";

    CareServiceAgreementRepository agreementRepository;
    ConsultationRequestRepository requestRepository;
    CareServicePackageRepository packageRepository;
    DoctorCareProfileRepository profileRepository;
    ConsultationRenewalRepository renewalRepository;

    @Override
    @Transactional
    public CareServiceAgreement createForReservation(ConsultationRequest request) {
        invalidateCurrent(request.getId(), "Replaced by a new Doctor reservation or material offer snapshot");
        CareServicePackage carePackage = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new AppException(ErrorCode.CARE_SERVICE_PACKAGE_NOT_FOUND));
        DoctorCareProfile profile = profileRepository.findByDoctorId(request.getAssignedDoctorId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_CARE_PROFILE_NOT_FOUND));

        CareServiceAgreement agreement = CareServiceAgreement.builder()
                .agreementType(CareServiceAgreementType.INITIAL_CARE)
                .requestId(request.getId())
                .memberId(request.getMemberId())
                .doctorId(request.getAssignedDoctorId())
                .packageId(carePackage.getId())
                .packageFamilyId(carePackage.getFamilyId())
                .packageCode(carePackage.getCode())
                .packageName(carePackage.getName())
                .packageVersion(carePackage.getVersionNumber())
                .serviceDescription(carePackage.getDescription())
                .includedServices(new ArrayList<>(carePackage.getIncludedServices()))
                .excludedServices(new ArrayList<>(carePackage.getExcludedServices()))
                .priceAmount(carePackage.getPriceAmount())
                .currency(carePackage.getCurrency())
                .durationDays(carePackage.getDurationDays())
                .startRule(CareStartRule.IMMEDIATE_AFTER_VERIFIED_PAYMENT)
                .supportScheduleSnapshotJson(profile.getAvailabilityJson())
                .supportTimezoneSnapshot(profile.getTimezone())
                .supportPolicy(carePackage.getSupportPolicy())
                .renewable(carePackage.getRenewable())
                .termsPolicyReference(defaultIfBlank(carePackage.getTermsPolicyReference(), DEFAULT_TERMS))
                .cancellationPolicyReference(CANCELLATION_POLICY)
                .refundPolicyReference(REFUND_POLICY)
                .emergencyLimitation(EMERGENCY_LIMITATION)
                .aiLimitation(AI_LIMITATION)
                .serviceLimitation(SERVICE_LIMITATION)
                .healthDataScopeDisclosure(HEALTH_DATA_SCOPE)
                .status(CareServiceAgreementStatus.PENDING_ACCEPTANCE)
                .validUntil(request.getPaymentDeadline())
                .build();
        return agreementRepository.saveAndFlush(agreement);
    }

    @Override
    @Transactional(readOnly = true)
    public CareServiceAgreementResponse getCurrentForMember(Long memberId, Long requestId) {
        ConsultationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        validateOwner(memberId, request);
        return agreementRepository.findFirstByRequestIdOrderByCreatedAtDesc(requestId)
                .map(this::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                        "Care Service Agreement not found"));
    }

    @Override
    @Transactional
    public CareServiceAgreementResponse accept(Long memberId, Long requestId, Long agreementId) {
        ConsultationRequest request = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        validateOwner(memberId, request);
        if (request.getStatus() != ConsultationRequestStatus.WAITING_ACCEPTANCE)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        CareServiceAgreement agreement = agreementRepository.findByIdAndMemberId(agreementId, memberId)
                .filter(candidate -> Objects.equals(candidate.getRequestId(), requestId))
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
        if (agreement.getStatus() != CareServiceAgreementStatus.PENDING_ACCEPTANCE
                || !Objects.equals(agreement.getDoctorId(), request.getAssignedDoctorId())
                || !agreement.getValidUntil().isAfter(Instant.now()))
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        Instant now = Instant.now();
        agreement.setStatus(CareServiceAgreementStatus.ACCEPTED);
        agreement.setAcceptedByMember(memberId);
        agreement.setAcceptedAt(now);
        agreementRepository.save(agreement);

        request.setStatus(ConsultationRequestStatus.WAITING_PAYMENT);
        requestRepository.save(request);
        return toResponse(agreement);
    }

    @Override
    @Transactional
    public CareServiceAgreement requireAcceptedForUpdate(ConsultationRequest request) {
        CareServiceAgreement agreement = agreementRepository
                .findFirstByRequestIdAndStatusInOrderByCreatedAtDesc(
                        request.getId(), List.of(CareServiceAgreementStatus.ACCEPTED))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                        "An accepted Care Service Agreement is required"));
        if (!Objects.equals(agreement.getMemberId(), request.getMemberId())
                || !Objects.equals(agreement.getDoctorId(), request.getAssignedDoctorId())
                || !agreement.getValidUntil().isAfter(Instant.now()))
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "The accepted Care Service Agreement is no longer valid");
        return agreement;
    }

    @Override
    @Transactional
    public void consume(CareServiceAgreement agreement) {
        if (agreement.getStatus() != CareServiceAgreementStatus.ACCEPTED)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);
        agreement.setStatus(CareServiceAgreementStatus.CONSUMED);
        agreement.setConsumedAt(Instant.now());
        agreementRepository.save(agreement);
    }

    @Override
    @Transactional
    public void invalidateCurrent(Long requestId, String reason) {
        agreementRepository.findFirstByRequestIdAndStatusInOrderByCreatedAtDesc(requestId, INVALIDATABLE_STATUSES)
                .ifPresent(agreement -> {
                    agreement.setStatus(CareServiceAgreementStatus.INVALIDATED);
                    agreement.setInvalidatedAt(Instant.now());
                    agreement.setInvalidationReason(reason);
                    agreementRepository.save(agreement);
                });
    }

    @Override
    @Transactional
    public CareServiceAgreement createForRenewal(
            ConsultationRenewal renewal, CareServicePackage carePackage, DoctorCareProfile profile) {
        CareServiceAgreement agreement = CareServiceAgreement.builder()
                .renewalId(renewal.getId())
                .agreementType(CareServiceAgreementType.RENEWAL)
                .memberId(renewal.getMemberId())
                .doctorId(renewal.getDoctorId())
                .packageId(carePackage.getId())
                .packageFamilyId(carePackage.getFamilyId())
                .packageCode(carePackage.getCode())
                .packageName(carePackage.getName())
                .packageVersion(carePackage.getVersionNumber())
                .serviceDescription(carePackage.getDescription())
                .includedServices(new ArrayList<>(carePackage.getIncludedServices()))
                .excludedServices(new ArrayList<>(carePackage.getExcludedServices()))
                .priceAmount(carePackage.getPriceAmount())
                .currency(carePackage.getCurrency())
                .durationDays(carePackage.getDurationDays())
                .extensionStartsAt(renewal.getPreviousEndsAt())
                .resultingEndsAt(renewal.getProposedNewEndsAt())
                .startRule(CareStartRule.EXTENSION_FROM_CURRENT_END)
                .supportScheduleSnapshotJson(profile.getAvailabilityJson())
                .supportTimezoneSnapshot(profile.getTimezone())
                .supportPolicy(carePackage.getSupportPolicy())
                .renewable(carePackage.getRenewable())
                .termsPolicyReference(defaultIfBlank(carePackage.getTermsPolicyReference(), DEFAULT_TERMS))
                .cancellationPolicyReference(CANCELLATION_POLICY)
                .refundPolicyReference(REFUND_POLICY)
                .emergencyLimitation(EMERGENCY_LIMITATION)
                .aiLimitation(AI_LIMITATION)
                .serviceLimitation(SERVICE_LIMITATION)
                .healthDataScopeDisclosure(HEALTH_DATA_SCOPE)
                .status(CareServiceAgreementStatus.PENDING_ACCEPTANCE)
                .validUntil(renewal.getPaymentDeadline())
                .build();
        return agreementRepository.saveAndFlush(agreement);
    }

    @Override
    @Transactional(readOnly = true)
    public CareServiceAgreementResponse getRenewalAgreement(Long memberId, Long renewalId) {
        ConsultationRenewal renewal = renewalRepository.findByIdAndMemberId(renewalId, memberId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
        return agreementRepository.findFirstByRenewalIdOrderByCreatedAtDesc(renewal.getId())
                .map(this::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                        "Renewal Agreement not found"));
    }

    @Override
    @Transactional
    public CareServiceAgreementResponse acceptRenewal(Long memberId, Long renewalId, Long agreementId) {
        ConsultationRenewal renewal = renewalRepository.findByIdForUpdate(renewalId)
                .filter(item -> Objects.equals(item.getMemberId(), memberId))
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
        if (renewal.getStatus() != ConsultationRenewalStatus.PENDING_ACCEPTANCE)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);
        CareServiceAgreement agreement = agreementRepository.findByIdAndMemberId(agreementId, memberId)
                .filter(item -> Objects.equals(item.getRenewalId(), renewalId))
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
        if (agreement.getAgreementType() != CareServiceAgreementType.RENEWAL
                || agreement.getStatus() != CareServiceAgreementStatus.PENDING_ACCEPTANCE
                || !agreement.getValidUntil().isAfter(Instant.now()))
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        Instant now = Instant.now();
        agreement.setStatus(CareServiceAgreementStatus.ACCEPTED);
        agreement.setAcceptedByMember(memberId);
        agreement.setAcceptedAt(now);
        agreementRepository.save(agreement);
        renewal.setStatus(ConsultationRenewalStatus.WAITING_PAYMENT);
        renewalRepository.save(renewal);
        return toResponse(agreement);
    }

    @Override
    @Transactional
    public CareServiceAgreement requireAcceptedForRenewal(ConsultationRenewal renewal) {
        CareServiceAgreement agreement = agreementRepository
                .findFirstByRenewalIdAndStatusInOrderByCreatedAtDesc(
                        renewal.getId(), List.of(CareServiceAgreementStatus.ACCEPTED))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                        "An accepted Renewal Agreement is required"));
        if (agreement.getAgreementType() != CareServiceAgreementType.RENEWAL
                || !Objects.equals(agreement.getMemberId(), renewal.getMemberId())
                || !Objects.equals(agreement.getDoctorId(), renewal.getDoctorId())
                || !agreement.getValidUntil().isAfter(Instant.now()))
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "The accepted Renewal Agreement is no longer valid");
        return agreement;
    }

    @Override
    @Transactional
    public void invalidateRenewal(Long renewalId, String reason) {
        agreementRepository.findFirstByRenewalIdAndStatusInOrderByCreatedAtDesc(
                        renewalId, INVALIDATABLE_STATUSES)
                .ifPresent(agreement -> {
                    agreement.setStatus(CareServiceAgreementStatus.INVALIDATED);
                    agreement.setInvalidatedAt(Instant.now());
                    agreement.setInvalidationReason(reason);
                    agreementRepository.save(agreement);
                });
    }

    private void validateOwner(Long memberId, ConsultationRequest request) {
        if (!Objects.equals(memberId, request.getMemberId()))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private CareServiceAgreementResponse toResponse(CareServiceAgreement agreement) {
        return CareServiceAgreementResponse.builder()
                .id(agreement.getId())
                .requestId(agreement.getRequestId())
                .renewalId(agreement.getRenewalId())
                .agreementType(agreement.getAgreementType())
                .memberId(agreement.getMemberId())
                .doctorId(agreement.getDoctorId())
                .packageId(agreement.getPackageId())
                .packageFamilyId(agreement.getPackageFamilyId())
                .packageCode(agreement.getPackageCode())
                .packageName(agreement.getPackageName())
                .packageVersion(agreement.getPackageVersion())
                .serviceDescription(agreement.getServiceDescription())
                .includedServices(List.copyOf(agreement.getIncludedServices()))
                .excludedServices(List.copyOf(agreement.getExcludedServices()))
                .priceAmount(agreement.getPriceAmount())
                .currency(agreement.getCurrency())
                .durationDays(agreement.getDurationDays())
                .extensionStartsAt(agreement.getExtensionStartsAt())
                .resultingEndsAt(agreement.getResultingEndsAt())
                .startRule(agreement.getStartRule())
                .supportScheduleSnapshotJson(agreement.getSupportScheduleSnapshotJson())
                .supportTimezoneSnapshot(agreement.getSupportTimezoneSnapshot())
                .supportPolicy(agreement.getSupportPolicy())
                .renewable(agreement.getRenewable())
                .termsPolicyReference(agreement.getTermsPolicyReference())
                .cancellationPolicyReference(agreement.getCancellationPolicyReference())
                .refundPolicyReference(agreement.getRefundPolicyReference())
                .emergencyLimitation(agreement.getEmergencyLimitation())
                .aiLimitation(agreement.getAiLimitation())
                .serviceLimitation(agreement.getServiceLimitation())
                .healthDataScopeDisclosure(agreement.getHealthDataScopeDisclosure())
                .status(agreement.getStatus())
                .acceptedByMember(agreement.getAcceptedByMember())
                .acceptedAt(agreement.getAcceptedAt())
                .validUntil(agreement.getValidUntil())
                .invalidatedAt(agreement.getInvalidatedAt())
                .invalidationReason(agreement.getInvalidationReason())
                .consumedAt(agreement.getConsumedAt())
                .createdAt(agreement.getCreatedAt())
                .build();
    }
}
