package fit.iuh.se.hschat.service.reservation.impl;

import fit.iuh.se.hschat.entity.CareServicePackage;
import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.DoctorReservation;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.*;
import fit.iuh.se.hschat.service.doctor.SupportScheduleValidator;
import fit.iuh.se.hschat.service.reservation.DoctorReservationService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.dto.command.NotificationIntent;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.service.OperationalEventService;
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
public class DoctorReservationServiceImpl implements DoctorReservationService {

    static final List<ConsultationStatus> CAPACITY_SESSION_STATUSES = List.of(
            ConsultationStatus.SCHEDULED,
            ConsultationStatus.ACTIVE
    );

    DoctorReservationRepository reservationRepository;
    ConsultationRequestRepository requestRepository;
    ConsultationSessionRepository sessionRepository;
    UserAccountRepository userAccountRepository;
    DoctorCareProfileRepository doctorCareProfileRepository;
    CareServicePackageRepository packageRepository;
    SupportScheduleValidator scheduleValidator;
    OperationalEventService operationalEventService;

    @Override
    @Transactional
    public DoctorReservation reserve(
            ConsultationRequest request,
            Long coordinatorId,
            Long doctorId,
            Instant expiresAt
    ) {
        if (request.getStatus() != ConsultationRequestStatus.PENDING_REVIEW)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);
        if (reservationRepository.findByRequestIdAndStatusForUpdate(
                request.getId(), DoctorReservationStatus.ACTIVE).isPresent())
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS, "Request already has an active doctor reservation");

        // The doctor row is the serialization point for all capacity-consuming reservations.
        UserAccount doctor = userAccountRepository.findByIdForUpdate(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
        List<DoctorIneligibilityReason> reasons = getIneligibilityReasons(request, doctor, Instant.now(), null);
        if (!reasons.isEmpty())
            throw new AppException(ErrorCode.DOCTOR_NOT_ELIGIBLE_FOR_CONSULTATION, "Doctor is not eligible: " + reasons);

        Instant now = Instant.now();
        DoctorReservation reservation = DoctorReservation.builder()
                .requestId(request.getId())
                .doctorId(doctorId)
                .packageId(request.getPackageId())
                .packageVersion(request.getPackageVersion())
                .reservedBy(coordinatorId)
                .reservedAt(now)
                .expiresAt(expiresAt)
                .status(DoctorReservationStatus.ACTIVE)
                .build();
        reservation = reservationRepository.saveAndFlush(reservation);
        operationalEventService.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.RESERVATION).domainId(reservation.getId())
                .eventType(BusinessEventType.DOCTOR_RESERVED).actorType(BusinessActorType.USER)
                .actorUserId(coordinatorId).actorRole(UserRole.CARE_COORDINATOR.name())
                .requestId(request.getId()).memberId(request.getMemberId()).doctorId(doctorId)
                .newState(DoctorReservationStatus.ACTIVE.name())
                .idempotencyKey("reservation:" + reservation.getId() + ":reserved").build());
        return reservation;
    }

    @Override
    @Transactional(readOnly = true)
    public long getEffectiveLoad(Long doctorId, Instant now) {
        return sessionRepository.countByDoctorIdAndStatusIn(doctorId, CAPACITY_SESSION_STATUSES)
                + reservationRepository.countByDoctorIdAndStatusAndExpiresAtAfter(
                doctorId, DoctorReservationStatus.ACTIVE, now);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorIneligibilityReason> getIneligibilityReasons(
            ConsultationRequest request,
            UserAccount doctor,
            Instant now,
            Long ownReservationId
    ) {
        List<DoctorIneligibilityReason> reasons = new ArrayList<>();
        if (doctor.getRole() != UserRole.DOCTOR)
            reasons.add(DoctorIneligibilityReason.NOT_DOCTOR);
        if (doctor.getStatus() != AccountStatus.ACTIVE)
            reasons.add(DoctorIneligibilityReason.ACCOUNT_INACTIVE);

        DoctorCareProfile profile = doctorCareProfileRepository.findByDoctorId(doctor.getId()).orElse(null);
        if (profile == null) {
            reasons.add(DoctorIneligibilityReason.PROFILE_MISSING);
            return reasons;
        }
        if (!Boolean.TRUE.equals(profile.getAcceptsOneOnOneCare()))
            reasons.add(DoctorIneligibilityReason.NOT_ACCEPTING_ONE_ON_ONE_CARE);
        if (profile.getSpecialty() == null)
            reasons.add(DoctorIneligibilityReason.SPECIALTY_MISSING);

        CareServicePackage carePackage = request.getPackageId() == null
                ? null
                : packageRepository.findById(request.getPackageId()).orElse(null);
        if (carePackage != null && carePackage.getRequiredSpecialty() != null
                && !Objects.equals(carePackage.getRequiredSpecialty(), profile.getSpecialty()))
            reasons.add(DoctorIneligibilityReason.SPECIALTY_MISMATCH);

        if (!scheduleValidator.isValid(
                profile.getAvailabilityJson(),
                profile.getTimezone(),
                Boolean.TRUE.equals(profile.getAcceptsOneOnOneCare())))
            reasons.add(DoctorIneligibilityReason.SUPPORT_SCHEDULE_INVALID);

        long reservations = ownReservationId == null
                ? reservationRepository.countByDoctorIdAndStatusAndExpiresAtAfter(
                doctor.getId(), DoctorReservationStatus.ACTIVE, now)
                : reservationRepository.countByDoctorIdAndStatusAndExpiresAtAfterAndIdNot(
                doctor.getId(), DoctorReservationStatus.ACTIVE, now, ownReservationId);
        long otherLoad = sessionRepository.countByDoctorIdAndStatusIn(doctor.getId(), CAPACITY_SESSION_STATUSES)
                + reservations;
        if (profile.getMaxActiveConsultations() == null
                || profile.getMaxActiveConsultations() <= 0
                || otherLoad >= profile.getMaxActiveConsultations())
            reasons.add(DoctorIneligibilityReason.CAPACITY_FULL);
        return reasons;
    }

    @Override
    @Transactional
    public boolean revalidateBeforePayment(ConsultationRequest request) {
        return revalidate(request, DoctorReservationReleaseReason.DOCTOR_INELIGIBLE_BEFORE_PAYMENT, false);
    }

    @Override
    @Transactional
    public boolean revalidateBeforeActivation(ConsultationRequest request) {
        return revalidate(request, DoctorReservationReleaseReason.DOCTOR_INELIGIBLE_BEFORE_ACTIVATION, true);
    }

    @Override
    @Transactional
    public void release(ConsultationRequest request, DoctorReservationReleaseReason reason) {
        reservationRepository.findByRequestIdAndStatusForUpdate(request.getId(), DoctorReservationStatus.ACTIVE)
                .ifPresent(reservation -> releaseReservation(reservation, reason, Instant.now()));
    }

    @Override
    @Transactional
    public void expireOverdueReservations(Instant now) {
        reservationRepository.findByStatusAndExpiresAtBefore(DoctorReservationStatus.ACTIVE, now)
                .forEach(reservation -> requestRepository.findByIdForUpdate(reservation.getRequestId())
                        .ifPresent(request -> {
                            releaseReservation(reservation, DoctorReservationReleaseReason.RESERVATION_EXPIRED, now);
                            request.setStatus(ConsultationRequestStatus.EXPIRED);
                            request.setExpiredAt(now);
                            clearCurrentAssignment(request);
                            requestRepository.save(request);
                        }));
    }

    private boolean revalidate(
            ConsultationRequest request,
            DoctorReservationReleaseReason failureReason,
            boolean requireNoMemberSession
    ) {
        DoctorReservation reservation = reservationRepository
                .findByRequestIdAndStatusForUpdate(request.getId(), DoctorReservationStatus.ACTIVE)
                .orElse(null);
        Instant now = Instant.now();
        if (reservation == null
                || !Objects.equals(reservation.getDoctorId(), request.getAssignedDoctorId())
                || !reservation.getExpiresAt().isAfter(now)) {
            if (reservation != null)
                releaseReservation(reservation, failureReason, now);
            returnToCoordination(request, failureReason);
            return false;
        }

        UserAccount doctor = userAccountRepository.findByIdForUpdate(reservation.getDoctorId()).orElse(null);
        boolean eligible = doctor != null
                && getIneligibilityReasons(request, doctor, now, reservation.getId()).isEmpty();
        boolean noConflict = !requireNoMemberSession
                || !sessionRepository.existsByMemberIdAndStatusIn(request.getMemberId(), CAPACITY_SESSION_STATUSES);
        if (eligible && noConflict)
            return true;

        releaseReservation(reservation, failureReason, now);
        returnToCoordination(request, failureReason);
        return false;
    }

    private void releaseReservation(
            DoctorReservation reservation,
            DoctorReservationReleaseReason reason,
            Instant now
    ) {
        reservation.setStatus(switch (reason) {
            case RESERVATION_EXPIRED -> DoctorReservationStatus.EXPIRED;
            case ACTIVATED -> DoctorReservationStatus.CONSUMED;
            default -> DoctorReservationStatus.RELEASED;
        });
        reservation.setReleaseReason(reason);
        reservation.setReleasedAt(now);
        reservationRepository.save(reservation);
        operationalEventService.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.RESERVATION).domainId(reservation.getId())
                .eventType(BusinessEventType.RESERVATION_RELEASED).actorType(BusinessActorType.SYSTEM)
                .requestId(reservation.getRequestId()).doctorId(reservation.getDoctorId())
                .previousState(DoctorReservationStatus.ACTIVE.name()).newState(reservation.getStatus().name())
                .reason(reason.name()).idempotencyKey("reservation:" + reservation.getId() + ":released:" + reason)
                .build());
    }

    private void returnToCoordination(
            ConsultationRequest request,
            DoctorReservationReleaseReason reason) {
        ConsultationRequestStatus previous = request.getStatus();
        Long previousDoctorId = request.getAssignedDoctorId();
        request.setStatus(ConsultationRequestStatus.PENDING_REVIEW);
        clearCurrentAssignment(request);
        requestRepository.save(request);
        operationalEventService.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.REQUEST).domainId(request.getId())
                .eventType(BusinessEventType.DOCTOR_RECOORDINATED).actorType(BusinessActorType.SYSTEM)
                .requestId(request.getId()).memberId(request.getMemberId()).doctorId(previousDoctorId)
                .previousState(previous.name()).newState(ConsultationRequestStatus.PENDING_REVIEW.name())
                .reason(reason.name())
                .idempotencyKey("request:" + request.getId() + ":doctor-recoordination:" + reason)
                .notifications(java.util.List.of(NotificationIntent.forRole(UserRole.CARE_COORDINATOR,
                        NotificationType.OPERATIONAL_REVIEW_REQUIRED, "Doctor coordination required",
                        "A care request requires a new Doctor reservation.", BusinessDomainType.REQUEST,
                        request.getId(), "request:" + request.getId() + ":doctor-recoordination:" + reason
                                + ":coordinators")))
                .build());
    }

    private void clearCurrentAssignment(ConsultationRequest request) {
        request.setAssignedDoctorId(null);
        request.setDoctorReservedAt(null);
        request.setPaymentDeadline(null);
    }
}
