package fit.iuh.se.hschat.service.reservation.impl;

import fit.iuh.se.hschat.entity.*;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.*;
import fit.iuh.se.hschat.service.doctor.SupportScheduleValidator;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorReservationServiceImplTest {

    @Mock DoctorReservationRepository reservationRepository;
    @Mock ConsultationRequestRepository requestRepository;
    @Mock ConsultationSessionRepository sessionRepository;
    @Mock UserAccountRepository userAccountRepository;
    @Mock DoctorCareProfileRepository profileRepository;
    @Mock CareServicePackageRepository packageRepository;
    @Mock SupportScheduleValidator scheduleValidator;

    DoctorReservationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DoctorReservationServiceImpl(
                reservationRepository,
                requestRepository,
                sessionRepository,
                userAccountRepository,
                profileRepository,
                packageRepository,
                scheduleValidator
        );
    }

    @Test
    void specialtyMismatchBlocksReservationEvenForPreferredDoctor() {
        ConsultationRequest request = request(100L);
        request.setPreferredDoctorId(2L);
        UserAccount doctor = doctor(AccountStatus.ACTIVE);
        stubEligibility(request, doctor, profile(DoctorSpecialty.CARDIOLOGY, true, 2),
                carePackage(DoctorSpecialty.INTERNAL_MEDICINE));
        when(reservationRepository.findByRequestIdAndStatusForUpdate(100L, DoctorReservationStatus.ACTIVE))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> service.reserve(request, 9L, 2L, Instant.now().plusSeconds(300)));

        assertTrue(exception.getMessage().contains(DoctorIneligibilityReason.SPECIALTY_MISMATCH.name()));
        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    void disabledDoctorAfterDiscoveryFailsAtReservation() {
        ConsultationRequest request = request(100L);
        UserAccount doctor = doctor(AccountStatus.INACTIVE);
        stubEligibility(request, doctor, profile(DoctorSpecialty.CARDIOLOGY, true, 2),
                carePackage(DoctorSpecialty.CARDIOLOGY));
        when(reservationRepository.findByRequestIdAndStatusForUpdate(100L, DoctorReservationStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(AppException.class,
                () -> service.reserve(request, 9L, 2L, Instant.now().plusSeconds(300)));
        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    void retryingReservationForSameRequestIsSafelyRejected() {
        ConsultationRequest request = request(100L);
        when(reservationRepository.findByRequestIdAndStatusForUpdate(100L, DoctorReservationStatus.ACTIVE))
                .thenReturn(Optional.of(reservation(500L, 100L)));

        assertThrows(AppException.class,
                () -> service.reserve(request, 9L, 2L, Instant.now().plusSeconds(300)));
        verify(userAccountRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    void twoRequestsCompetingForFinalSlotCannotExceedCapacity() throws Exception {
        ReentrantLock doctorLock = new ReentrantLock();
        AtomicInteger activeReservations = new AtomicInteger();
        UserAccount doctor = doctor(AccountStatus.ACTIVE);
        DoctorCareProfile profile = profile(DoctorSpecialty.CARDIOLOGY, true, 1);
        CareServicePackage carePackage = carePackage(DoctorSpecialty.CARDIOLOGY);

        when(reservationRepository.findByRequestIdAndStatusForUpdate(anyLong(), eq(DoctorReservationStatus.ACTIVE)))
                .thenReturn(Optional.empty());
        when(userAccountRepository.findByIdForUpdate(2L)).thenAnswer(invocation -> {
            doctorLock.lock();
            return Optional.of(doctor);
        });
        when(profileRepository.findByDoctorId(2L)).thenReturn(Optional.of(profile));
        when(packageRepository.findById(10L)).thenReturn(Optional.of(carePackage));
        when(scheduleValidator.isValid(anyString(), anyString(), eq(true))).thenReturn(true);
        when(sessionRepository.countByDoctorIdAndStatusIn(eq(2L), anyCollection())).thenReturn(0L);
        when(reservationRepository.countByDoctorIdAndStatusAndExpiresAtAfter(
                eq(2L), eq(DoctorReservationStatus.ACTIVE), any(Instant.class)))
                .thenAnswer(invocation -> {
                    int count = activeReservations.get();
                    if (count > 0)
                        doctorLock.unlock();
                    return (long) count;
                });
        when(reservationRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            DoctorReservation saved = invocation.getArgument(0);
            saved.setId(500L + activeReservations.incrementAndGet());
            doctorLock.unlock();
            return saved;
        });

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch start = new CountDownLatch(1);
            Future<Boolean> first = executor.submit(() -> reserveAfter(start, request(100L)));
            Future<Boolean> second = executor.submit(() -> reserveAfter(start, request(101L)));
            start.countDown();

            assertEquals(1, List.of(first.get(), second.get()).stream().filter(Boolean::booleanValue).count());
            assertEquals(1, activeReservations.get());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void expiredReservationReleasesCapacityAndExpiresRequest() {
        Instant now = Instant.now();
        ConsultationRequest request = request(100L);
        request.setStatus(ConsultationRequestStatus.WAITING_PAYMENT);
        DoctorReservation reservation = reservation(500L, 100L);
        reservation.setExpiresAt(now.minusSeconds(1));
        when(reservationRepository.findByStatusAndExpiresAtBefore(DoctorReservationStatus.ACTIVE, now))
                .thenReturn(List.of(reservation));
        when(requestRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(request));

        service.expireOverdueReservations(now);

        assertEquals(DoctorReservationStatus.EXPIRED, reservation.getStatus());
        assertEquals(DoctorReservationReleaseReason.RESERVATION_EXPIRED, reservation.getReleaseReason());
        assertEquals(ConsultationRequestStatus.EXPIRED, request.getStatus());
        assertNull(request.getAssignedDoctorId());
    }

    @Test
    void disabledDoctorAtT2ReleasesReservationAndReturnsRequestToCoordination() {
        ConsultationRequest request = assignedRequest();
        DoctorReservation reservation = reservation(500L, 100L);
        when(reservationRepository.findByRequestIdAndStatusForUpdate(100L, DoctorReservationStatus.ACTIVE))
                .thenReturn(Optional.of(reservation));
        UserAccount doctor = doctor(AccountStatus.INACTIVE);
        stubEligibility(request, doctor, profile(DoctorSpecialty.CARDIOLOGY, true, 2),
                carePackage(DoctorSpecialty.CARDIOLOGY));

        assertFalse(service.revalidateBeforePayment(request));
        assertEquals(DoctorReservationStatus.RELEASED, reservation.getStatus());
        assertEquals(DoctorReservationReleaseReason.DOCTOR_INELIGIBLE_BEFORE_PAYMENT,
                reservation.getReleaseReason());
        assertEquals(ConsultationRequestStatus.PENDING_REVIEW, request.getStatus());
        assertNull(request.getAssignedDoctorId());
    }

    @Test
    void doctorBecomingIneligibleBetweenT2AndT3FailsFinalGuard() {
        ConsultationRequest request = assignedRequest();
        DoctorReservation reservation = reservation(500L, 100L);
        UserAccount doctor = doctor(AccountStatus.ACTIVE);
        when(reservationRepository.findByRequestIdAndStatusForUpdate(100L, DoctorReservationStatus.ACTIVE))
                .thenReturn(Optional.of(reservation));
        when(userAccountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(doctor));
        when(profileRepository.findByDoctorId(2L)).thenReturn(
                Optional.of(profile(DoctorSpecialty.CARDIOLOGY, true, 2)),
                Optional.of(profile(DoctorSpecialty.CARDIOLOGY, false, 2)));
        when(packageRepository.findById(10L)).thenReturn(Optional.of(carePackage(DoctorSpecialty.CARDIOLOGY)));
        when(scheduleValidator.isValid(anyString(), anyString(), anyBoolean())).thenReturn(true);
        when(sessionRepository.countByDoctorIdAndStatusIn(eq(2L), anyCollection())).thenReturn(0L);
        when(reservationRepository.countByDoctorIdAndStatusAndExpiresAtAfterAndIdNot(
                eq(2L), eq(DoctorReservationStatus.ACTIVE), any(Instant.class), eq(500L))).thenReturn(0L);
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);

        assertTrue(service.revalidateBeforePayment(request));
        assertFalse(service.revalidateBeforeActivation(request));
        assertEquals(ConsultationRequestStatus.PENDING_REVIEW, request.getStatus());
        assertEquals(DoctorReservationReleaseReason.DOCTOR_INELIGIBLE_BEFORE_ACTIVATION,
                reservation.getReleaseReason());
    }

    @Test
    void t3CapacityExcludesOwnReservation() {
        ConsultationRequest request = assignedRequest();
        DoctorReservation reservation = reservation(500L, 100L);
        UserAccount doctor = doctor(AccountStatus.ACTIVE);
        when(reservationRepository.findByRequestIdAndStatusForUpdate(100L, DoctorReservationStatus.ACTIVE))
                .thenReturn(Optional.of(reservation));
        stubEligibility(request, doctor, profile(DoctorSpecialty.CARDIOLOGY, true, 1),
                carePackage(DoctorSpecialty.CARDIOLOGY));
        when(reservationRepository.countByDoctorIdAndStatusAndExpiresAtAfterAndIdNot(
                eq(2L), eq(DoctorReservationStatus.ACTIVE), any(Instant.class), eq(500L))).thenReturn(0L);
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);

        assertTrue(service.revalidateBeforeActivation(request));
        verify(reservationRepository).countByDoctorIdAndStatusAndExpiresAtAfterAndIdNot(
                eq(2L), eq(DoctorReservationStatus.ACTIVE), any(Instant.class), eq(500L));
        verify(reservationRepository, never()).countByDoctorIdAndStatusAndExpiresAtAfter(
                eq(2L), eq(DoctorReservationStatus.ACTIVE), any(Instant.class));
    }

    private boolean reserveAfter(CountDownLatch start, ConsultationRequest request) throws InterruptedException {
        start.await();
        try {
            service.reserve(request, 9L, 2L, Instant.now().plusSeconds(300));
            return true;
        } catch (AppException exception) {
            return false;
        }
    }

    private void stubEligibility(
            ConsultationRequest request,
            UserAccount doctor,
            DoctorCareProfile profile,
            CareServicePackage carePackage
    ) {
        when(userAccountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(doctor));
        when(profileRepository.findByDoctorId(2L)).thenReturn(Optional.of(profile));
        when(packageRepository.findById(request.getPackageId())).thenReturn(Optional.of(carePackage));
        when(scheduleValidator.isValid(anyString(), anyString(), anyBoolean())).thenReturn(true);
        when(sessionRepository.countByDoctorIdAndStatusIn(eq(2L), anyCollection())).thenReturn(0L);
    }

    private ConsultationRequest request(Long id) {
        return ConsultationRequest.builder()
                .id(id)
                .memberId(1L)
                .packageId(10L)
                .packageVersion(1)
                .status(ConsultationRequestStatus.PENDING_REVIEW)
                .build();
    }

    private ConsultationRequest assignedRequest() {
        ConsultationRequest request = request(100L);
        request.setStatus(ConsultationRequestStatus.WAITING_PAYMENT);
        request.setAssignedDoctorId(2L);
        request.setDoctorReservedAt(Instant.now());
        request.setPaymentDeadline(Instant.now().plusSeconds(300));
        return request;
    }

    private DoctorReservation reservation(Long id, Long requestId) {
        return DoctorReservation.builder()
                .id(id)
                .requestId(requestId)
                .doctorId(2L)
                .reservedBy(9L)
                .reservedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .status(DoctorReservationStatus.ACTIVE)
                .build();
    }

    private UserAccount doctor(AccountStatus status) {
        return UserAccount.builder()
                .id(2L)
                .email("doctor@example.com")
                .role(UserRole.DOCTOR)
                .status(status)
                .build();
    }

    private DoctorCareProfile profile(DoctorSpecialty specialty, boolean accepts, int capacity) {
        return DoctorCareProfile.builder()
                .doctorId(2L)
                .specialty(specialty)
                .acceptsOneOnOneCare(accepts)
                .maxActiveConsultations(capacity)
                .availabilityJson("{\"monday\":[{\"start\":\"09:00\",\"end\":\"17:00\"}]}")
                .timezone("Asia/Ho_Chi_Minh")
                .build();
    }

    private CareServicePackage carePackage(DoctorSpecialty specialty) {
        return CareServicePackage.builder()
                .id(10L)
                .versionNumber(1)
                .requiredSpecialty(specialty)
                .build();
    }
}
