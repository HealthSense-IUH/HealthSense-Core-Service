package fit.iuh.se.hschat.service.renewal.impl;

import fit.iuh.se.hschat.dto.request.DecideConsultationRenewalRequest;
import fit.iuh.se.hschat.dto.request.RequestConsultationRenewalRequest;
import fit.iuh.se.hschat.dto.response.ConsultationRenewalResponse;
import fit.iuh.se.hschat.dto.response.SessionExtensionResponse;
import fit.iuh.se.hschat.entity.*;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.*;
import fit.iuh.se.hschat.service.agreement.CareServiceAgreementService;
import fit.iuh.se.hschat.service.doctor.SupportScheduleValidator;
import fit.iuh.se.hschat.service.renewal.ConsultationRenewalService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationRenewalServiceImpl implements ConsultationRenewalService {

    static final List<ConsultationRenewalStatus> UNRESOLVED = List.of(
            ConsultationRenewalStatus.REQUESTED,
            ConsultationRenewalStatus.UNDER_REVIEW,
            ConsultationRenewalStatus.APPROVED,
            ConsultationRenewalStatus.PENDING_ACCEPTANCE,
            ConsultationRenewalStatus.WAITING_PAYMENT,
            ConsultationRenewalStatus.PAID,
            ConsultationRenewalStatus.REQUIRES_REVIEW);
    static final List<ConsultationRenewalStatus> CAPACITY_HOLDS = List.of(
            ConsultationRenewalStatus.APPROVED,
            ConsultationRenewalStatus.PENDING_ACCEPTANCE,
            ConsultationRenewalStatus.WAITING_PAYMENT,
            ConsultationRenewalStatus.PAID);
    static final List<ConsultationStatus> CAPACITY_SESSIONS = List.of(
            ConsultationStatus.ACTIVE, ConsultationStatus.SCHEDULED);

    ConsultationRenewalRepository renewalRepository;
    SessionExtensionRepository extensionRepository;
    ConsultationSessionRepository sessionRepository;
    CareServicePackageRepository packageRepository;
    DoctorCareProfileRepository profileRepository;
    DoctorReservationRepository reservationRepository;
    UserAccountRepository userAccountRepository;
    ConsultationPaymentRepository paymentRepository;
    CareServiceAgreementService agreementService;
    SupportScheduleValidator scheduleValidator;

    @NonFinal
    @Value("${app.consultation.renewal-payment-window-minutes:30}")
    long paymentWindowMinutes;

    @Override
    @Transactional
    public ConsultationRenewalResponse request(
            Long memberId, Long sessionId, RequestConsultationRenewalRequest request) {
        ConsultationSession session = sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        requireOwnedActiveSession(memberId, session);
        CareServicePackage originalPackage = packageRepository.findById(session.getPackageId())
                .orElseThrow(() -> new AppException(ErrorCode.CARE_SERVICE_PACKAGE_NOT_FOUND));
        if (!Boolean.TRUE.equals(originalPackage.getRenewable()))
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS, "Current package is not renewable");
        if (request != null && request.getPackageFamilyId() != null
                && !Objects.equals(request.getPackageFamilyId(), originalPackage.getFamilyId()))
            throw new AppException(ErrorCode.INVALID_PARAMETER,
                    "Renewal cannot switch package/service family");
        if (renewalRepository.existsBySessionIdAndStatusIn(sessionId, UNRESOLVED))
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Session already has an unresolved renewal");

        ConsultationRenewal renewal = renewalRepository.saveAndFlush(ConsultationRenewal.builder()
                .sessionId(session.getId())
                .memberId(session.getMemberId())
                .doctorId(session.getDoctorId())
                .packageFamilyId(originalPackage.getFamilyId())
                .status(ConsultationRenewalStatus.REQUESTED)
                .requestedAt(Instant.now())
                .build());
        return toResponse(renewal);
    }

    @Override
    @Transactional
    public ConsultationRenewalResponse beginReview(Long actorId, UserRole role, Long renewalId) {
        requireCoordinator(role);
        ConsultationRenewal renewal = renewalRepository.findByIdForUpdate(renewalId)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Renewal not found"));
        if (renewal.getStatus() != ConsultationRenewalStatus.REQUESTED)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);
        ConsultationSession session = sessionRepository.findByIdForUpdate(renewal.getSessionId())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        requireStillSameActiveEpisode(renewal, session);
        renewal.setStatus(ConsultationRenewalStatus.UNDER_REVIEW);
        renewal.setReviewedBy(actorId);
        renewal.setReviewStartedAt(Instant.now());
        return toResponse(renewalRepository.save(renewal));
    }

    @Override
    @Transactional
    public ConsultationRenewalResponse decide(
            Long actorId, UserRole role, Long renewalId, DecideConsultationRenewalRequest request) {
        requireCoordinator(role);
        ConsultationRenewal renewal = renewalRepository.findByIdForUpdate(renewalId)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Renewal not found"));
        if (renewal.getStatus() != ConsultationRenewalStatus.UNDER_REVIEW)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);
        if (!Boolean.TRUE.equals(request.getApproved())) {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank())
                throw new AppException(ErrorCode.INVALID_PARAMETER, "Rejection reason is required");
            renewal.setStatus(ConsultationRenewalStatus.REJECTED);
            renewal.setReviewedBy(actorId);
            renewal.setReviewedAt(Instant.now());
            renewal.setRejectionReason(request.getRejectionReason().trim());
            return toResponse(renewalRepository.save(renewal));
        }

        ConsultationSession session = sessionRepository.findByIdForUpdate(renewal.getSessionId())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        requireStillSameActiveEpisode(renewal, session);
        CareServicePackage carePackage;
        DoctorCareProfile profile;
        try {
            carePackage = packageRepository
                    .findByFamilyIdAndStatus(renewal.getPackageFamilyId(), CareServicePackageStatus.ACTIVE)
                    .filter(item -> Boolean.TRUE.equals(item.getRenewable()))
                    .orElseThrow(() -> new AppException(ErrorCode.CARE_SERVICE_PACKAGE_NOT_FOUND,
                            "No renewable ACTIVE package version exists for this family"));
            profile = validateFutureCapacity(renewal, session, carePackage, Instant.now());
        } catch (AppException exception) {
            renewal.setStatus(ConsultationRenewalStatus.REJECTED);
            renewal.setReviewedBy(actorId);
            renewal.setReviewedAt(Instant.now());
            renewal.setRejectionReason("Doctor is not eligible, available, or within capacity for the extension window");
            return toResponse(renewalRepository.save(renewal));
        }

        Instant now = Instant.now();
        Instant previousEndsAt = session.getEndsAt();
        Instant proposedNewEndsAt = previousEndsAt.plus(carePackage.getDurationDays(), ChronoUnit.DAYS);
        Instant deadline = now.plus(paymentWindowMinutes, ChronoUnit.MINUTES);
        if (deadline.isAfter(previousEndsAt)) deadline = previousEndsAt;
        if (!deadline.isAfter(now))
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Session ends too soon to complete a renewal payment");

        renewal.setStatus(ConsultationRenewalStatus.APPROVED);
        renewal.setReviewedBy(actorId);
        renewal.setReviewedAt(now);
        renewal.setPackageId(carePackage.getId());
        renewal.setPackageVersion(carePackage.getVersionNumber());
        renewal.setDurationDays(carePackage.getDurationDays());
        renewal.setPriceAmount(carePackage.getPriceAmount());
        renewal.setCurrency(carePackage.getCurrency());
        renewal.setSupportScheduleSnapshotJson(profile.getAvailabilityJson());
        renewal.setSupportTimezoneSnapshot(profile.getTimezone());
        renewal.setPreviousEndsAt(previousEndsAt);
        renewal.setProposedNewEndsAt(proposedNewEndsAt);
        renewal.setPaymentDeadline(deadline);
        renewalRepository.saveAndFlush(renewal);

        CareServiceAgreement agreement = agreementService.createForRenewal(renewal, carePackage, profile);
        renewal.setAgreementId(agreement.getId());
        renewal.setStatus(ConsultationRenewalStatus.PENDING_ACCEPTANCE);
        return toResponse(renewalRepository.save(renewal));
    }

    @Override
    @Transactional
    public ConsultationRenewalResponse cancel(Long memberId, Long renewalId) {
        ConsultationRenewal renewal = renewalRepository.findByIdForUpdate(renewalId)
                .filter(item -> Objects.equals(item.getMemberId(), memberId))
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
        if (!UNRESOLVED.contains(renewal.getStatus()) || renewal.getStatus() == ConsultationRenewalStatus.PAID)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);
        renewal.setStatus(ConsultationRenewalStatus.CANCELLED);
        agreementService.invalidateRenewal(renewalId, "Renewal cancelled by Member");
        return toResponse(renewalRepository.save(renewal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationRenewalResponse> getMemberSessionRenewals(Long memberId, Long sessionId) {
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        requireOwner(memberId, session);
        return renewalRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionExtensionResponse> getMemberSessionExtensions(Long memberId, Long sessionId) {
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        requireOwner(memberId, session);
        return extensionRepository.findBySessionIdOrderByAppliedAtAsc(sessionId).stream()
                .map(this::toExtensionResponse).toList();
    }

    @Override
    @Transactional
    public ConsultationRenewal requireWaitingPaymentForUpdate(Long memberId, Long renewalId) {
        ConsultationRenewal renewal = renewalRepository.findByIdForUpdate(renewalId)
                .filter(item -> Objects.equals(item.getMemberId(), memberId))
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
        if (renewal.getStatus() != ConsultationRenewalStatus.WAITING_PAYMENT
                || renewal.getPaymentDeadline() == null
                || !renewal.getPaymentDeadline().isAfter(Instant.now()))
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);
        ConsultationSession session = sessionRepository.findByIdForUpdate(renewal.getSessionId())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        requireStillSameActiveEpisode(renewal, session);
        return renewal;
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationRenewal requireOwned(Long memberId, Long renewalId) {
        return renewalRepository.findByIdAndMemberId(renewalId, memberId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
    }

    @Override
    @Transactional
    public void applyVerifiedPayment(ConsultationPayment payment, Instant now) {
        ConsultationRenewal renewal = renewalRepository.findByIdForUpdate(payment.getRenewalId())
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Renewal not found"));
        ConsultationSession session = sessionRepository.findByIdForUpdate(renewal.getSessionId())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        if (renewal.getStatus() != ConsultationRenewalStatus.WAITING_PAYMENT
                || session.getStatus() != ConsultationStatus.ACTIVE
                || !Objects.equals(session.getMemberId(), renewal.getMemberId())
                || !Objects.equals(session.getDoctorId(), renewal.getDoctorId())
                || !Objects.equals(session.getEndsAt(), renewal.getPreviousEndsAt())) {
            markReview(renewal, payment, now);
            return;
        }
        CareServiceAgreement agreement;
        try {
            agreement = agreementService.requireAcceptedForRenewal(renewal);
        } catch (AppException exception) {
            markReview(renewal, payment, now);
            return;
        }
        if (!Objects.equals(agreement.getId(), payment.getAgreementId())
                || agreement.getPriceAmount().compareTo(payment.getAmount()) != 0
                || !agreement.getCurrency().equalsIgnoreCase(payment.getCurrency())
                || !Objects.equals(agreement.getResultingEndsAt(), renewal.getProposedNewEndsAt())) {
            markReview(renewal, payment, now);
            return;
        }
        CareServicePackage carePackage = packageRepository.findById(renewal.getPackageId())
                .orElseThrow(() -> new AppException(ErrorCode.CARE_SERVICE_PACKAGE_NOT_FOUND));
        try {
            validateFutureCapacity(renewal, session, carePackage, now);
        } catch (AppException exception) {
            markReview(renewal, payment, now);
            return;
        }
        if (extensionRepository.existsByRenewalId(renewal.getId())) {
            payment.setStatus(ConsultationPaymentStatus.PAID);
            if (payment.getPaidAt() == null) payment.setPaidAt(now);
            paymentRepository.save(payment);
            return;
        }

        renewal.setStatus(ConsultationRenewalStatus.PAID);
        payment.setStatus(ConsultationPaymentStatus.PAID);
        payment.setPaidAt(now);
        paymentRepository.save(payment);
        SessionExtension extension = extensionRepository.saveAndFlush(SessionExtension.builder()
                .sessionId(session.getId())
                .renewalId(renewal.getId())
                .agreementId(agreement.getId())
                .paymentId(payment.getId())
                .previousEndsAt(renewal.getPreviousEndsAt())
                .newEndsAt(renewal.getProposedNewEndsAt())
                .durationDays(renewal.getDurationDays())
                .packageId(renewal.getPackageId())
                .packageVersion(renewal.getPackageVersion())
                .priceAmount(renewal.getPriceAmount())
                .currency(renewal.getCurrency())
                .supportScheduleSnapshotJson(renewal.getSupportScheduleSnapshotJson())
                .supportTimezoneSnapshot(renewal.getSupportTimezoneSnapshot())
                .appliedAt(now)
                .build());

        session.setEndsAt(extension.getNewEndsAt());
        session.setSupportEndsAt(extension.getNewEndsAt());
        session.setSupportScheduleSnapshotJson(extension.getSupportScheduleSnapshotJson());
        session.setSupportTimezoneSnapshot(extension.getSupportTimezoneSnapshot());
        session.setPackageId(extension.getPackageId());
        session.setPackageVersion(extension.getPackageVersion());
        session.setPackagePriceSnapshot(extension.getPriceAmount());
        session.setPackageDurationDaysSnapshot(extension.getDurationDays());
        sessionRepository.save(session);
        agreementService.consume(agreement);
        renewal.setSuccessfulPaymentId(payment.getId());
        renewal.setAppliedAt(now);
        renewal.setStatus(ConsultationRenewalStatus.APPLIED);
        renewalRepository.save(renewal);
    }

    @Override
    @Transactional
    public void expireForPayment(ConsultationPayment payment, Instant now) {
        if (payment.getRenewalId() == null) return;
        renewalRepository.findByIdForUpdate(payment.getRenewalId()).ifPresent(renewal -> {
            if (renewal.getStatus() == ConsultationRenewalStatus.WAITING_PAYMENT) {
                renewal.setStatus(ConsultationRenewalStatus.EXPIRED);
                renewalRepository.save(renewal);
                agreementService.invalidateRenewal(renewal.getId(), "Renewal payment expired or failed");
            }
        });
    }

    @Override
    @Transactional
    public void markRequiresReview(ConsultationPayment payment) {
        if (payment.getRenewalId() == null) return;
        renewalRepository.findByIdForUpdate(payment.getRenewalId()).ifPresent(renewal -> {
            if (renewal.getStatus() != ConsultationRenewalStatus.APPLIED) {
                renewal.setStatus(ConsultationRenewalStatus.REQUIRES_REVIEW);
                renewalRepository.save(renewal);
            }
        });
    }

    @Override
    @Transactional
    public void expireOverdueRenewals(Instant now) {
        renewalRepository.findByStatusInAndPaymentDeadlineBefore(
                        List.of(ConsultationRenewalStatus.PENDING_ACCEPTANCE,
                                ConsultationRenewalStatus.WAITING_PAYMENT), now)
                .forEach(candidate -> renewalRepository.findByIdForUpdate(candidate.getId())
                        .ifPresent(renewal -> {
                            if ((renewal.getStatus() == ConsultationRenewalStatus.PENDING_ACCEPTANCE
                                    || renewal.getStatus() == ConsultationRenewalStatus.WAITING_PAYMENT)
                                    && renewal.getPaymentDeadline() != null
                                    && !renewal.getPaymentDeadline().isAfter(now)) {
                                renewal.setStatus(ConsultationRenewalStatus.EXPIRED);
                                renewalRepository.save(renewal);
                                agreementService.invalidateRenewal(
                                        renewal.getId(), "Renewal agreement/payment window expired");
                            }
                        }));
    }

    private DoctorCareProfile validateFutureCapacity(
            ConsultationRenewal renewal,
            ConsultationSession session,
            CareServicePackage carePackage,
            Instant now) {
        UserAccount doctor = userAccountRepository.findByIdForUpdate(renewal.getDoctorId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
        DoctorCareProfile profile = profileRepository.findByDoctorId(doctor.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_CARE_PROFILE_NOT_FOUND));
        if (doctor.getRole() != UserRole.DOCTOR
                || doctor.getStatus() != AccountStatus.ACTIVE
                || !Boolean.TRUE.equals(profile.getAcceptsOneOnOneCare())
                || profile.getSpecialty() == null
                || (carePackage.getRequiredSpecialty() != null
                    && carePackage.getRequiredSpecialty() != profile.getSpecialty())
                || !scheduleValidator.isValid(profile.getAvailabilityJson(), profile.getTimezone(), true))
            throw new AppException(ErrorCode.DOCTOR_NOT_ELIGIBLE_FOR_CONSULTATION);

        Instant windowStart = renewal.getPreviousEndsAt() == null ? session.getEndsAt() : renewal.getPreviousEndsAt();
        Instant windowEnd = renewal.getProposedNewEndsAt() == null
                ? windowStart.plus(carePackage.getDurationDays(), ChronoUnit.DAYS)
                : renewal.getProposedNewEndsAt();
        long otherSessions = sessionRepository
                .countByDoctorIdAndIdNotAndStatusInAndStartedAtLessThanAndEndsAtGreaterThan(
                        doctor.getId(), session.getId(), CAPACITY_SESSIONS, windowEnd, windowStart);
        long reservations = reservationRepository.countByDoctorIdAndStatusAndExpiresAtAfter(
                doctor.getId(), DoctorReservationStatus.ACTIVE, now);
        long extensionHolds = renewal.getId() == null ? 0 : renewalRepository
                .countByDoctorIdAndSessionIdNotAndStatusInAndPreviousEndsAtLessThanAndProposedNewEndsAtGreaterThan(
                        doctor.getId(), session.getId(), CAPACITY_HOLDS, windowEnd, windowStart);
        if (profile.getMaxActiveConsultations() == null
                || profile.getMaxActiveConsultations() <= 0
                || otherSessions + reservations + extensionHolds >= profile.getMaxActiveConsultations())
            throw new AppException(ErrorCode.DOCTOR_CAPACITY_EXCEEDED);
        return profile;
    }

    private void markReview(ConsultationRenewal renewal, ConsultationPayment payment, Instant now) {
        renewal.setStatus(ConsultationRenewalStatus.REQUIRES_REVIEW);
        renewalRepository.save(renewal);
        payment.setStatus(ConsultationPaymentStatus.REQUIRES_REVIEW);
        if (payment.getPaidAt() == null) payment.setPaidAt(now);
        paymentRepository.save(payment);
    }

    private void requireOwnedActiveSession(Long memberId, ConsultationSession session) {
        requireOwner(memberId, session);
        if (session.getStatus() != ConsultationStatus.ACTIVE)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Only ACTIVE sessions can be renewed");
    }

    private void requireOwner(Long memberId, ConsultationSession session) {
        if (!Objects.equals(session.getMemberId(), memberId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
    }

    private void requireStillSameActiveEpisode(ConsultationRenewal renewal, ConsultationSession session) {
        if (session.getStatus() != ConsultationStatus.ACTIVE
                || !Objects.equals(session.getMemberId(), renewal.getMemberId())
                || !Objects.equals(session.getDoctorId(), renewal.getDoctorId()))
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Renewal must keep the same ACTIVE Member, Doctor and Session");
    }

    private void requireCoordinator(UserRole role) {
        if (role != UserRole.CARE_COORDINATOR && role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN)
            throw new AppException(ErrorCode.ACCESS_DENIED);
    }

    private ConsultationRenewalResponse toResponse(ConsultationRenewal renewal) {
        return ConsultationRenewalResponse.builder()
                .id(renewal.getId()).sessionId(renewal.getSessionId())
                .memberId(renewal.getMemberId()).doctorId(renewal.getDoctorId())
                .packageFamilyId(renewal.getPackageFamilyId()).packageId(renewal.getPackageId())
                .packageVersion(renewal.getPackageVersion()).durationDays(renewal.getDurationDays())
                .priceAmount(renewal.getPriceAmount()).currency(renewal.getCurrency())
                .supportScheduleSnapshotJson(renewal.getSupportScheduleSnapshotJson())
                .supportTimezoneSnapshot(renewal.getSupportTimezoneSnapshot())
                .previousEndsAt(renewal.getPreviousEndsAt()).proposedNewEndsAt(renewal.getProposedNewEndsAt())
                .agreementId(renewal.getAgreementId()).successfulPaymentId(renewal.getSuccessfulPaymentId())
                .status(renewal.getStatus()).requestedAt(renewal.getRequestedAt())
                .reviewedBy(renewal.getReviewedBy()).reviewStartedAt(renewal.getReviewStartedAt())
                .reviewedAt(renewal.getReviewedAt()).rejectionReason(renewal.getRejectionReason())
                .paymentDeadline(renewal.getPaymentDeadline()).appliedAt(renewal.getAppliedAt())
                .createdAt(renewal.getCreatedAt()).updatedAt(renewal.getUpdatedAt()).build();
    }

    private SessionExtensionResponse toExtensionResponse(SessionExtension extension) {
        return SessionExtensionResponse.builder()
                .id(extension.getId()).sessionId(extension.getSessionId()).renewalId(extension.getRenewalId())
                .agreementId(extension.getAgreementId()).paymentId(extension.getPaymentId())
                .previousEndsAt(extension.getPreviousEndsAt()).newEndsAt(extension.getNewEndsAt())
                .durationDays(extension.getDurationDays()).packageId(extension.getPackageId())
                .packageVersion(extension.getPackageVersion()).priceAmount(extension.getPriceAmount())
                .currency(extension.getCurrency())
                .supportScheduleSnapshotJson(extension.getSupportScheduleSnapshotJson())
                .supportTimezoneSnapshot(extension.getSupportTimezoneSnapshot())
                .appliedAt(extension.getAppliedAt()).build();
    }
}
