package fit.iuh.se.hschat.service.request.impl;

import fit.iuh.se.hschat.dto.request.ApproveConsultationRequest;
import fit.iuh.se.hschat.dto.request.CreateConsultationRequest;
import fit.iuh.se.hschat.dto.request.RequestMoreConsultationInfoRequest;
import fit.iuh.se.hschat.dto.request.RejectConsultationRequest;
import fit.iuh.se.hschat.dto.request.SubmitConsultationMoreInfoRequest;
import fit.iuh.se.hschat.dto.response.ConsultationRequestResponse;
import fit.iuh.se.hschat.dto.response.DoctorCandidateResponse;
import fit.iuh.se.hschat.entity.CareServicePackage;
import fit.iuh.se.hschat.entity.ConsultationMoreInfoCycle;
import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.DoctorReservation;
import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
import fit.iuh.se.hschat.entity.enums.DoctorIneligibilityReason;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import fit.iuh.se.hschat.entity.enums.DoctorReservationReleaseReason;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.CareServicePackageRepository;
import fit.iuh.se.hschat.repository.ConsultationMoreInfoCycleRepository;
import fit.iuh.se.hschat.repository.ConsultationRequestRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.DoctorCareProfileRepository;
import fit.iuh.se.hschat.service.reservation.DoctorReservationService;
import fit.iuh.se.hschat.service.agreement.CareServiceAgreementService;
import fit.iuh.se.hschat.service.payment.PaymentCancellationService;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationRequestServiceImplTest {

    @Mock
    ConsultationRequestRepository requestRepository;
    @Mock
    ConsultationMoreInfoCycleRepository moreInfoCycleRepository;
    @Mock
    ConsultationSessionRepository sessionRepository;
    @Mock
    HealthRecordRepository healthRecordRepository;
    @Mock
    UserAccountRepository userAccountRepository;
    @Mock
    CareServicePackageRepository packageRepository;
    @Mock
    DoctorCareProfileRepository doctorCareProfileRepository;
    @Mock
    DoctorReservationService reservationService;
    @Mock
    CareServiceAgreementService agreementService;
    @Mock
    PaymentCancellationService paymentCancellationService;
    @Mock
    ConsultationMapper mapper;
    @Mock
    fit.iuh.se.hsoperations.event.OperationalEventPublisher OperationalEventPublisher;

    ConsultationRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConsultationRequestServiceImpl(
                requestRepository,
                moreInfoCycleRepository,
                sessionRepository,
                healthRecordRepository,
                userAccountRepository,
                packageRepository,
                doctorCareProfileRepository,
                reservationService,
                agreementService,
                paymentCancellationService,
                mapper,
                OperationalEventPublisher
        );
        ReflectionTestUtils.setField(service, "paymentDeadlineMinutes", 30L);
        lenient().when(reservationService.reserve(any(), anyLong(), anyLong(), any(Instant.class)))
                .thenAnswer(invocation -> DoctorReservation.builder()
                        .requestId(((ConsultationRequest) invocation.getArgument(0)).getId())
                        .doctorId(invocation.getArgument(2))
                        .reservedAt(Instant.now())
                        .expiresAt(invocation.getArgument(3))
                        .build());
    }

    @Test
    void createRequestStoresPackageSnapshotAndPendingReview() {
        when(userAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(requestRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(packageRepository.findByIdAndStatus(10L, CareServicePackageStatus.ACTIVE))
                .thenReturn(Optional.of(carePackage()));
        when(requestRepository.save(any(ConsultationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toRequestResponse(any(ConsultationRequest.class)))
                .thenReturn(ConsultationRequestResponse.builder().status(ConsultationRequestStatus.PENDING_REVIEW).build());

        CreateConsultationRequest request = new CreateConsultationRequest();
        request.setPackageId(10L);
        request.setReasonForCare("Need monitoring");
        request.setCurrentConcern("Recurring palpitations");

        service.createRequest(1L, request);

        ArgumentCaptor<ConsultationRequest> captor = ArgumentCaptor.forClass(ConsultationRequest.class);
        verify(requestRepository).save(captor.capture());
        ConsultationRequest saved = captor.getValue();
        assertEquals(ConsultationRequestStatus.PENDING_REVIEW, saved.getStatus());
        assertEquals(10L, saved.getPackageId());
        assertEquals(3, saved.getPackageVersion());
        assertEquals(new BigDecimal("399000.00"), saved.getPackagePriceSnapshot());
        assertEquals(7, saved.getPackageDurationDaysSnapshot());
        verify(OperationalEventPublisher).record(argThat(command ->
                command.eventType() == fit.iuh.se.hsoperations.entity.enums.BusinessEventType.REQUEST_CREATED
                        && Long.valueOf(1L).equals(command.actorUserId())
                        && "PENDING_REVIEW".equals(command.newState())));
    }

    @Test
    void createRequestRejectsMemberWithUnresolvedRequest() {
        when(userAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(requestRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(true);

        CreateConsultationRequest request = new CreateConsultationRequest();
        request.setPackageId(10L);
        request.setReasonForCare("Need monitoring");
        request.setCurrentConcern("Recurring palpitations");

        assertThrows(AppException.class, () -> service.createRequest(1L, request));
        verify(packageRepository, never()).findByIdAndStatus(anyLong(), any());
        verify(requestRepository, never()).save(any());
    }

    @Test
    void approveRequestReservesDoctorWithoutCreatingSession() {
        ConsultationRequest existing = ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .status(ConsultationRequestStatus.PENDING_REVIEW)
                .build();

        when(requestRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(requestRepository.save(any(ConsultationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationService.reserve(eq(existing), eq(9L), eq(2L), any(Instant.class)))
                .thenAnswer(invocation -> {
                    Instant deadline = invocation.getArgument(3);
                    return DoctorReservation.builder()
                            .requestId(existing.getId())
                            .doctorId(2L)
                            .reservedAt(deadline.minus(30, ChronoUnit.MINUTES))
                            .expiresAt(deadline)
                            .build();
                });
        when(mapper.toRequestResponse(any(ConsultationRequest.class)))
                .thenReturn(ConsultationRequestResponse.builder().status(ConsultationRequestStatus.WAITING_ACCEPTANCE).build());

        ApproveConsultationRequest request = new ApproveConsultationRequest();
        request.setDoctorId(2L);

        service.approveRequest(9L, UserRole.CARE_COORDINATOR, 100L, request);

        ArgumentCaptor<ConsultationRequest> captor = ArgumentCaptor.forClass(ConsultationRequest.class);
        verify(requestRepository).save(captor.capture());
        ConsultationRequest saved = captor.getValue();
        assertEquals(ConsultationRequestStatus.WAITING_ACCEPTANCE, saved.getStatus());
        assertNotNull(saved.getIntakeFrozenAt());
        assertEquals(2L, saved.getAssignedDoctorId());
        assertNotNull(saved.getDoctorReservedAt());
        assertNotNull(saved.getPaymentDeadline());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void twoCoordinatorsApprovingSameRequestOnlyOneTransitionWins() throws Exception {
        ConsultationRequest shared = ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .status(ConsultationRequestStatus.PENDING_REVIEW)
                .build();
        ReentrantLock requestLock = new ReentrantLock();
        when(requestRepository.findByIdForUpdate(100L)).thenAnswer(invocation -> {
            requestLock.lock();
            return Optional.of(shared);
        });
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(requestRepository.save(shared)).thenAnswer(invocation -> {
            requestLock.unlock();
            return shared;
        });
        when(mapper.toRequestResponse(shared)).thenReturn(
                ConsultationRequestResponse.builder().status(ConsultationRequestStatus.WAITING_ACCEPTANCE).build());

        ApproveConsultationRequest approval = new ApproveConsultationRequest();
        approval.setDoctorId(2L);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch start = new CountDownLatch(1);
            Callable<Boolean> task = () -> {
                start.await();
                try {
                    service.approveRequest(9L, UserRole.CARE_COORDINATOR, 100L, approval);
                    return true;
                } catch (AppException exception) {
                    return false;
                }
            };
            Future<Boolean> first = executor.submit(task);
            Future<Boolean> second = executor.submit(task);
            start.countDown();

            assertEquals(1, List.of(first.get(), second.get()).stream().filter(Boolean::booleanValue).count());
            verify(reservationService, times(1)).reserve(any(), eq(9L), eq(2L), any(Instant.class));
        } finally {
            executor.shutdownNow();
        }
    }

    @ParameterizedTest
    @EnumSource(value = ConsultationRequestStatus.class, names = {
            "PENDING_REVIEW", "NEED_MORE_INFO", "WAITING_ACCEPTANCE", "WAITING_PAYMENT"})
    void cancellationBeforeActivationReleasesReservation(ConsultationRequestStatus initialStatus) {
        ConsultationRequest request = ConsultationRequest.builder()
                .id(100L).memberId(1L).status(initialStatus).build();
        when(requestRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);
        when(mapper.toRequestResponse(request)).thenReturn(ConsultationRequestResponse.builder().build());

        service.cancelMyRequest(1L, 100L);

        verify(reservationService).release(request, DoctorReservationReleaseReason.MEMBER_CANCELLED);
        verify(agreementService).invalidateCurrent(100L, "Member cancelled before care activation");
        verify(paymentCancellationService).prepareRequestCancellation(100L);
        verify(paymentCancellationService).cancelProviderLinksAfterCommit(100L);
        assertEquals(ConsultationRequestStatus.CANCELLED, request.getStatus());
    }

    @Test
    void activatedRequestCannotBeCancelledAndMustUseSessionTermination() {
        ConsultationRequest request = ConsultationRequest.builder()
                .id(100L).memberId(1L).status(ConsultationRequestStatus.FULFILLED).build();
        when(requestRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(request));

        assertThrows(AppException.class, () -> service.cancelMyRequest(1L, 100L));

        verify(reservationService, never()).release(any(), any());
        verify(agreementService, never()).invalidateCurrent(anyLong(), anyString());
        verify(paymentCancellationService, never()).prepareRequestCancellation(anyLong());
    }

    @Test
    void rejectionBeforeActivationReleasesReservation() {
        ConsultationRequest request = ConsultationRequest.builder()
                .id(100L).memberId(1L).status(ConsultationRequestStatus.WAITING_ACCEPTANCE).build();
        when(requestRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);
        when(mapper.toRequestResponse(request)).thenReturn(ConsultationRequestResponse.builder().build());
        RejectConsultationRequest rejection = new RejectConsultationRequest();
        rejection.setRejectionReason("Assignment cannot proceed");

        service.rejectRequest(9L, UserRole.CARE_COORDINATOR, 100L, rejection);

        verify(reservationService).release(request, DoctorReservationReleaseReason.COORDINATOR_REJECTED);
        assertEquals(ConsultationRequestStatus.REJECTED, request.getStatus());
    }

    @Test
    void approveRequestCountsActiveScheduledSessionsAndReservationsForCapacity() {
        ConsultationRequest existing = ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .status(ConsultationRequestStatus.PENDING_REVIEW)
                .build();

        when(requestRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(reservationService.reserve(any(), eq(9L), eq(2L), any(Instant.class)))
                .thenThrow(new AppException(fit.iuh.se.hsshared.advice.entity.enums.ErrorCode.DOCTOR_CAPACITY_EXCEEDED));

        ApproveConsultationRequest request = new ApproveConsultationRequest();
        request.setDoctorId(2L);

        assertThrows(AppException.class, () -> service.approveRequest(9L, UserRole.CARE_COORDINATOR, 100L, request));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void approveRequestDoesNotCountExpiredWaitingPaymentReservationsForCapacity() {
        ConsultationRequest existing = ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .status(ConsultationRequestStatus.PENDING_REVIEW)
                .build();

        when(requestRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(requestRepository.save(any(ConsultationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toRequestResponse(any(ConsultationRequest.class)))
                .thenReturn(ConsultationRequestResponse.builder().status(ConsultationRequestStatus.WAITING_ACCEPTANCE).build());

        ApproveConsultationRequest request = new ApproveConsultationRequest();
        request.setDoctorId(2L);

        service.approveRequest(9L, UserRole.CARE_COORDINATOR, 100L, request);

        verify(reservationService).reserve(any(), eq(9L), eq(2L), any(Instant.class));
        verify(requestRepository).save(any(ConsultationRequest.class));
    }

    @Test
    void approveRequestCalculatesPaymentDeadlineOnBackend() {
        ConsultationRequest existing = ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .status(ConsultationRequestStatus.PENDING_REVIEW)
                .build();

        when(requestRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(requestRepository.save(any(ConsultationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationService.reserve(eq(existing), eq(9L), eq(2L), any(Instant.class)))
                .thenAnswer(invocation -> {
                    Instant deadline = invocation.getArgument(3);
                    return DoctorReservation.builder()
                            .requestId(existing.getId())
                            .doctorId(2L)
                            .reservedAt(deadline.minus(30, ChronoUnit.MINUTES))
                            .expiresAt(deadline)
                            .build();
                });
        when(mapper.toRequestResponse(any(ConsultationRequest.class)))
                .thenReturn(ConsultationRequestResponse.builder().status(ConsultationRequestStatus.WAITING_ACCEPTANCE).build());

        ApproveConsultationRequest request = new ApproveConsultationRequest();
        request.setDoctorId(2L);

        service.approveRequest(9L, UserRole.CARE_COORDINATOR, 100L, request);

        ArgumentCaptor<ConsultationRequest> captor = ArgumentCaptor.forClass(ConsultationRequest.class);
        verify(requestRepository).save(captor.capture());
        assertEquals(30L, ChronoUnit.MINUTES.between(
                captor.getValue().getDoctorReservedAt(),
                captor.getValue().getPaymentDeadline()
        ));
    }

    @Test
    void doctorCandidatesKeepPreferredDoctorVisibleWhenIneligible() {
        ConsultationRequest existing = ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .preferredDoctorId(2L)
                .status(ConsultationRequestStatus.PENDING_REVIEW)
                .build();
        UserAccount preferredDoctor = user(2L, UserRole.DOCTOR);

        when(requestRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.findDoctors(
                eq(UserRole.DOCTOR),
                eq(AccountStatus.ACTIVE),
                any(PageRequest.class))
        ).thenReturn(new PageImpl<>(List.of(preferredDoctor), PageRequest.of(0, 10), 1));
        when(doctorCareProfileRepository.findByDoctorIdIn(List.of(2L)))
                .thenReturn(List.of(doctorProfile(1)));
        when(reservationService.getEffectiveLoad(eq(2L), any(Instant.class))).thenReturn(1L);
        when(reservationService.getIneligibilityReasons(
                eq(existing), eq(preferredDoctor), any(Instant.class), isNull()))
                .thenReturn(List.of(DoctorIneligibilityReason.CAPACITY_FULL));

        PageResponse<DoctorCandidateResponse> response = service.getDoctorCandidates(
                UserRole.CARE_COORDINATOR,
                100L,
                null,
                null,
                false,
                PageRequest.of(0, 10)
        );

        assertEquals(1, response.getContent().size());
        DoctorCandidateResponse candidate = response.getContent().getFirst();
        assertEquals(2L, candidate.getDoctorId());
        assertTrue(candidate.getPreferredByMember());
        assertFalse(candidate.getEligible());
        assertTrue(candidate.getIneligibleReasons().contains(DoctorIneligibilityReason.CAPACITY_FULL));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void approveRequestRejectsDoctorThatDoesNotAcceptOneOnOneCare() {
        ConsultationRequest existing = ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .status(ConsultationRequestStatus.PENDING_REVIEW)
                .build();

        when(requestRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(reservationService.reserve(any(), eq(9L), eq(2L), any(Instant.class)))
                .thenThrow(new AppException(
                        fit.iuh.se.hsshared.advice.entity.enums.ErrorCode.DOCTOR_NOT_ELIGIBLE_FOR_CONSULTATION));

        ApproveConsultationRequest request = new ApproveConsultationRequest();
        request.setDoctorId(2L);

        assertThrows(AppException.class, () -> service.approveRequest(9L, UserRole.CARE_COORDINATOR, 100L, request));
        verify(requestRepository, never()).save(any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void approveRequestDtoDoesNotExposePaymentDeadlineToClient() {
        boolean hasPaymentDeadlineField = Arrays.stream(ApproveConsultationRequest.class.getDeclaredFields())
                .anyMatch(field -> "paymentDeadline".equals(field.getName()));

        assertFalse(hasPaymentDeadlineField);
    }

    @Test
    void createRequestRequiresV3IntakeFields() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        CreateConsultationRequest request = new CreateConsultationRequest();
        request.setPackageId(10L);

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> "reasonForCare".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "currentConcern".equals(v.getPropertyPath().toString())));
    }

    @Test
    void createRequestPersistsMultipleOwnedHealthRecordReferences() {
        stubSuccessfulCreate();
        when(healthRecordRepository.findByIdAndUserId(11L, 1L)).thenReturn(Optional.of(healthRecord(11L, 1L)));
        when(healthRecordRepository.findByIdAndUserId(12L, 1L)).thenReturn(Optional.of(healthRecord(12L, 1L)));
        CreateConsultationRequest request = validCreateRequest();
        request.setSelectedHealthRecordIds(List.of(11L, 12L));

        service.createRequest(1L, request);

        ArgumentCaptor<ConsultationRequest> captor = ArgumentCaptor.forClass(ConsultationRequest.class);
        verify(requestRepository).save(captor.capture());
        assertEquals(List.of(11L, 12L), captor.getValue().getSelectedHealthRecordIds());
        assertEquals(11L, captor.getValue().getHealthRecordId());
    }

    @Test
    void createRequestRejectsSelectedRecordOwnedByAnotherMember() {
        when(userAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(packageRepository.findByIdAndStatus(10L, CareServicePackageStatus.ACTIVE))
                .thenReturn(Optional.of(carePackage()));
        when(healthRecordRepository.findByIdAndUserId(11L, 1L)).thenReturn(Optional.empty());
        CreateConsultationRequest request = validCreateRequest();
        request.setSelectedHealthRecordIds(List.of(11L));

        assertThrows(AppException.class, () -> service.createRequest(1L, request));

        verify(requestRepository, never()).save(any());
    }

    @Test
    void repeatedNeedMoreInfoCyclesPreserveCompleteHistoryAndReturnToPendingReview() {
        ConsultationRequest existing = ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .reason("Care reason")
                .reasonForCare("Care reason")
                .currentConcern("Current concern")
                .status(ConsultationRequestStatus.PENDING_REVIEW)
                .build();
        List<ConsultationMoreInfoCycle> history = new CopyOnWriteArrayList<>();
        when(requestRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(existing));
        when(requestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toRequestResponse(any())).thenAnswer(invocation -> ConsultationRequestResponse.builder()
                .status(invocation.<ConsultationRequest>getArgument(0).getStatus())
                .build());
        when(moreInfoCycleRepository.save(any())).thenAnswer(invocation -> {
            ConsultationMoreInfoCycle cycle = invocation.getArgument(0);
            if (!history.contains(cycle)) {
                cycle.setId((long) history.size() + 1);
                history.add(cycle);
            }
            return cycle;
        });
        when(moreInfoCycleRepository.findByRequestIdOrderByRequestedAtAsc(100L)).thenReturn(history);
        when(moreInfoCycleRepository.findFirstByRequestIdAndRespondedAtIsNullOrderByRequestedAtDesc(100L))
                .thenAnswer(invocation -> history.stream()
                        .filter(cycle -> cycle.getRespondedAt() == null)
                        .reduce((first, second) -> second));

        requestAndSubmitMoreInfo(existing, "Please describe symptoms", "Symptoms occur nightly");
        requestAndSubmitMoreInfo(existing, "Please clarify medication", "No current medication");

        assertEquals(ConsultationRequestStatus.PENDING_REVIEW, existing.getStatus());
        assertEquals(2, history.size());
        assertEquals("Please describe symptoms", history.get(0).getCoordinatorMessage());
        assertEquals("Symptoms occur nightly", history.get(0).getMemberResponse());
        assertNotNull(history.get(0).getRespondedAt());
        assertEquals("Please clarify medication", history.get(1).getCoordinatorMessage());
        assertEquals("No current medication", history.get(1).getMemberResponse());
    }

    @Test
    void frozenIntakeRejectsFurtherMoreInfoMutation() {
        ConsultationRequest frozen = ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .status(ConsultationRequestStatus.NEED_MORE_INFO)
                .intakeFrozenAt(Instant.now())
                .build();
        when(requestRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(frozen));

        assertThrows(AppException.class, () -> service.submitMoreInfo(
                1L, 100L, new SubmitConsultationMoreInfoRequest()));

        verify(moreInfoCycleRepository, never()).save(any());
    }

    @Test
    void waitingAcceptanceIsPartOfGlobalUnresolvedGate() {
        assertTrue(ConsultationRequestServiceImpl.UNRESOLVED_REQUEST_STATUSES
                .contains(ConsultationRequestStatus.WAITING_ACCEPTANCE));
    }

    @Test
    void unresolvedRequestBlocksCreationForDifferentPackage() {
        when(userAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(requestRepository.existsByMemberIdAndStatusIn(eq(1L), argThat(statuses ->
                statuses.contains(ConsultationRequestStatus.WAITING_ACCEPTANCE)))).thenReturn(true);

        assertThrows(AppException.class, () -> service.createRequest(1L, validCreateRequest()));

        verify(packageRepository, never()).findByIdAndStatus(anyLong(), any());
    }

    @Test
    void unresolvedRequestBlocksCreationRegardlessOfPackageSpecialty() {
        when(userAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(requestRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(true);
        CreateConsultationRequest cardiologyPackageRequest = validCreateRequest();
        cardiologyPackageRequest.setPackageId(999L);

        assertThrows(AppException.class, () -> service.createRequest(1L, cardiologyPackageRequest));

        verify(packageRepository, never()).findByIdAndStatus(anyLong(), any());
    }

    @Test
    void activeSessionGloballyBlocksNewRequest() {
        assertBusySessionBlocksCreation();
    }

    @Test
    void scheduledSessionGloballyBlocksNewRequest() {
        assertBusySessionBlocksCreation();
        assertTrue(ConsultationRequestServiceImpl.MEMBER_BUSY_SESSION_STATUSES
                .containsAll(List.of(fit.iuh.se.hschat.entity.enums.ConsultationStatus.ACTIVE,
                        fit.iuh.se.hschat.entity.enums.ConsultationStatus.SCHEDULED)));
    }

    @Test
    void terminalRequestAllowsFutureRequestWhenMemberHasNoBusySession() {
        stubSuccessfulCreate();

        service.createRequest(1L, validCreateRequest());

        verify(requestRepository).save(any(ConsultationRequest.class));
    }

    @Test
    void concurrentCreatesForSameMemberCannotBothPersistUnresolvedRequests() throws Exception {
        ReentrantLock memberTransactionLock = new ReentrantLock();
        AtomicBoolean unresolvedExists = new AtomicBoolean(false);
        when(userAccountRepository.findByIdForUpdate(1L)).thenAnswer(invocation -> {
            memberTransactionLock.lock();
            return Optional.of(user(1L, UserRole.MEMBER));
        });
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(requestRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenAnswer(invocation -> {
            boolean exists = unresolvedExists.get();
            if (exists)
                memberTransactionLock.unlock();
            return exists;
        });
        when(packageRepository.findByIdAndStatus(10L, CareServicePackageStatus.ACTIVE))
                .thenReturn(Optional.of(carePackage()));
        when(requestRepository.save(any())).thenAnswer(invocation -> {
            unresolvedExists.set(true);
            memberTransactionLock.unlock();
            return invocation.getArgument(0);
        });
        when(mapper.toRequestResponse(any())).thenReturn(ConsultationRequestResponse.builder().build());
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> create = () -> {
            start.await();
            try {
                service.createRequest(1L, validCreateRequest());
                return true;
            } catch (AppException exception) {
                return false;
            }
        };

        Future<Boolean> first = executor.submit(create);
        Future<Boolean> second = executor.submit(create);
        start.countDown();
        long successes = List.of(first.get(5, TimeUnit.SECONDS), second.get(5, TimeUnit.SECONDS))
                .stream().filter(Boolean::booleanValue).count();
        executor.shutdownNow();

        assertEquals(1, successes);
        verify(requestRepository, times(1)).save(any(ConsultationRequest.class));
    }

    private void requestAndSubmitMoreInfo(ConsultationRequest request, String coordinatorMessage, String response) {
        RequestMoreConsultationInfoRequest needMoreInfo = new RequestMoreConsultationInfoRequest();
        needMoreInfo.setReason(coordinatorMessage);
        service.requestMoreInfo(9L, UserRole.CARE_COORDINATOR, request.getId(), needMoreInfo);

        SubmitConsultationMoreInfoRequest submission = new SubmitConsultationMoreInfoRequest();
        submission.setResponseNote(response);
        service.submitMoreInfo(request.getMemberId(), request.getId(), submission);
    }

    private void assertBusySessionBlocksCreation() {
        reset(userAccountRepository, sessionRepository, requestRepository);
        when(userAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), argThat(statuses -> statuses.size() == 2)))
                .thenReturn(true);

        assertThrows(AppException.class, () -> service.createRequest(1L, validCreateRequest()));
        verify(requestRepository, never()).save(any());
    }

    private void stubSuccessfulCreate() {
        when(userAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(requestRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(packageRepository.findByIdAndStatus(10L, CareServicePackageStatus.ACTIVE))
                .thenReturn(Optional.of(carePackage()));
        when(requestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toRequestResponse(any())).thenReturn(ConsultationRequestResponse.builder().build());
    }

    private CreateConsultationRequest validCreateRequest() {
        CreateConsultationRequest request = new CreateConsultationRequest();
        request.setPackageId(10L);
        request.setReasonForCare("Need monitoring");
        request.setCurrentConcern("Recurring palpitations");
        return request;
    }

    private HealthRecord healthRecord(Long id, Long userId) {
        return HealthRecord.builder()
                .id(id)
                .userId(userId)
                .fileName("record.pdf")
                .s3FileKey("records/" + id)
                .build();
    }

    private UserAccount user(Long id, UserRole role) {
        return UserAccount.builder()
                .id(id)
                .email(id + "@example.com")
                .passwordHash("hash")
                .role(role)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    private CareServicePackage carePackage() {
        return CareServicePackage.builder()
                .id(10L)
                .code("PERSONAL_CARE_7D")
                .versionNumber(3)
                .name("Personal Care - 7 Days")
                .priceAmount(new BigDecimal("399000.00"))
                .durationDays(7)
                .renewable(true)
                .status(CareServicePackageStatus.ACTIVE)
                .build();
    }

    private DoctorCareProfile doctorProfile(int maxActiveConsultations) {
        return doctorProfile(maxActiveConsultations, true);
    }

    private DoctorCareProfile doctorProfile(int maxActiveConsultations, boolean acceptsOneOnOneCare) {
        return DoctorCareProfile.builder()
                .doctorId(2L)
                .specialty(DoctorSpecialty.CARDIOLOGY)
                .acceptsOneOnOneCare(acceptsOneOnOneCare)
                .maxActiveConsultations(maxActiveConsultations)
                .availabilityJson("{\"weekly\":[{\"dayOfWeek\":\"MONDAY\",\"start\":\"07:00\",\"end\":\"11:00\"}]}")
                .timezone("Asia/Ho_Chi_Minh")
                .build();
    }
}
