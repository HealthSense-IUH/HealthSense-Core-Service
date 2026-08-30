package fit.iuh.se.hschat.service.renewal.impl;

import fit.iuh.se.hschat.dto.request.DecideConsultationRenewalRequest;
import fit.iuh.se.hschat.dto.request.RequestConsultationRenewalRequest;
import fit.iuh.se.hschat.entity.*;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.*;
import fit.iuh.se.hschat.service.agreement.CareServiceAgreementService;
import fit.iuh.se.hschat.service.doctor.SupportScheduleValidator;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationRenewalServiceImplTest {

    @Mock ConsultationRenewalRepository renewalRepository;
    @Mock SessionExtensionRepository extensionRepository;
    @Mock ConsultationSessionRepository sessionRepository;
    @Mock CareServicePackageRepository packageRepository;
    @Mock DoctorCareProfileRepository profileRepository;
    @Mock DoctorReservationRepository reservationRepository;
    @Mock UserAccountRepository userAccountRepository;
    @Mock ConsultationPaymentRepository paymentRepository;
    @Mock CareServiceAgreementService agreementService;
    @Mock SupportScheduleValidator scheduleValidator;
    @Mock fit.iuh.se.hsoperations.service.OperationalEventService operationalEventService;

    ConsultationRenewalServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConsultationRenewalServiceImpl(
                renewalRepository, extensionRepository, sessionRepository, packageRepository,
                profileRepository, reservationRepository, userAccountRepository, paymentRepository,
                agreementService, scheduleValidator, operationalEventService);
        ReflectionTestUtils.setField(service, "paymentWindowMinutes", 30L);
        lenient().when(renewalRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(renewalRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(extensionRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(sessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void onlyActiveOwnedSessionCanRequestAndCompletedSessionNeverReopens() {
        ConsultationSession completed = session(ConsultationStatus.COMPLETED);
        when(sessionRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(completed));

        assertThrows(AppException.class,
                () -> service.request(1L, 10L, new RequestConsultationRenewalRequest()));
        assertEquals(ConsultationStatus.COMPLETED, completed.getStatus());
        verify(renewalRepository, never()).saveAndFlush(any());
    }

    @Test
    void requestRejectsFamilySwitchAndSecondUnresolvedRenewal() {
        assertTrue(ConsultationRenewalServiceImpl.UNRESOLVED
                .contains(ConsultationRenewalStatus.REQUIRES_REVIEW));
        ConsultationSession session = session(ConsultationStatus.ACTIVE);
        when(sessionRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(session));
        when(packageRepository.findById(100L)).thenReturn(Optional.of(currentPackage()));

        RequestConsultationRenewalRequest switched = new RequestConsultationRenewalRequest();
        switched.setPackageFamilyId(999L);
        assertThrows(AppException.class, () -> service.request(1L, 10L, switched));

        when(renewalRepository.existsBySessionIdAndStatusIn(eq(10L), anyCollection())).thenReturn(true);
        RequestConsultationRenewalRequest sameFamily = new RequestConsultationRenewalRequest();
        sameFamily.setPackageFamilyId(50L);
        assertThrows(AppException.class, () -> service.request(1L, 10L, sameFamily));
        verify(renewalRepository, never()).saveAndFlush(any());
    }

    @Test
    void approvalKeepsEpisodeAndSnapshotsCurrentActiveVersionAndPrice() {
        ConsultationSession session = session(ConsultationStatus.ACTIVE);
        ConsultationRenewal renewal = requestedRenewal();
        CareServicePackage current = currentPackage();
        DoctorCareProfile profile = eligibleProfile(2);
        CareServiceAgreement agreement = renewalAgreement();
        when(renewalRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(renewal));
        when(sessionRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(session));
        when(packageRepository.findByFamilyIdAndStatus(50L, CareServicePackageStatus.ACTIVE))
                .thenReturn(Optional.of(current));
        stubCapacity(profile, 0, 0, 0);
        when(agreementService.createForRenewal(renewal, current, profile)).thenReturn(agreement);

        var response = service.decide(9L, UserRole.CARE_COORDINATOR, 20L,
                new DecideConsultationRenewalRequest(true, null));

        assertEquals(ConsultationRenewalStatus.PENDING_ACCEPTANCE, response.getStatus());
        assertEquals(10L, response.getSessionId());
        assertEquals(1L, response.getMemberId());
        assertEquals(2L, response.getDoctorId());
        assertEquals(101L, response.getPackageId());
        assertEquals(2, response.getPackageVersion());
        assertEquals(new BigDecimal("150000"), response.getPriceAmount());
        assertEquals(session.getEndsAt(), response.getPreviousEndsAt());
        assertEquals(session.getEndsAt().plus(30, ChronoUnit.DAYS), response.getProposedNewEndsAt());
        verify(sessionRepository).countByDoctorIdAndIdNotAndStatusInAndStartedAtLessThanAndEndsAtGreaterThan(
                eq(2L), eq(10L), anyCollection(), any(), any());
    }

    @Test
    void approvalRejectsUnavailableOrFullDoctorWithoutChangingSession() {
        ConsultationSession session = session(ConsultationStatus.ACTIVE);
        Instant originalEnd = session.getEndsAt();
        ConsultationRenewal renewal = requestedRenewal();
        when(renewalRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(renewal));
        when(sessionRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(session));
        when(packageRepository.findByFamilyIdAndStatus(50L, CareServicePackageStatus.ACTIVE))
                .thenReturn(Optional.of(currentPackage()));
        stubCapacity(eligibleProfile(1), 1, 0, 0);

        var response = service.decide(9L, UserRole.CARE_COORDINATOR, 20L,
                new DecideConsultationRenewalRequest(true, null));
        assertEquals(ConsultationRenewalStatus.REJECTED, response.getStatus());
        assertEquals(originalEnd, session.getEndsAt());
        verify(agreementService, never()).createForRenewal(any(), any(), any());
    }

    @Test
    void memberCancellationReleasesUnresolvedRenewalWithoutChangingSession() {
        ConsultationSession session = session(ConsultationStatus.ACTIVE);
        ConsultationRenewal renewal = waitingPaymentRenewal(session);
        Instant originalEnd = session.getEndsAt();
        when(renewalRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(renewal));

        var response = service.cancel(1L, 20L);

        assertEquals(ConsultationRenewalStatus.CANCELLED, response.getStatus());
        assertEquals(originalEnd, session.getEndsAt());
        verify(agreementService).invalidateRenewal(20L, "Renewal cancelled by Member");
    }

    @Test
    void currentSessionDoesNotDoubleCountAgainstDoctorCapacity() {
        ConsultationSession session = session(ConsultationStatus.ACTIVE);
        ConsultationRenewal renewal = requestedRenewal();
        CareServicePackage current = currentPackage();
        DoctorCareProfile profile = eligibleProfile(1);
        when(renewalRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(renewal));
        when(sessionRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(session));
        when(packageRepository.findByFamilyIdAndStatus(50L, CareServicePackageStatus.ACTIVE))
                .thenReturn(Optional.of(current));
        stubCapacity(profile, 0, 0, 0);
        when(agreementService.createForRenewal(any(), any(), any())).thenReturn(renewalAgreement());

        assertDoesNotThrow(() -> service.decide(9L, UserRole.CARE_COORDINATOR, 20L,
                new DecideConsultationRenewalRequest(true, null)));
    }

    @Test
    void verifiedPaymentCreatesHistoryBeforeApplyingEndDateAndPreservesBothDates() {
        ConsultationSession session = session(ConsultationStatus.ACTIVE);
        ConsultationRenewal renewal = waitingPaymentRenewal(session);
        ConsultationPayment payment = renewalPayment();
        CareServiceAgreement agreement = acceptedRenewalAgreement(renewal);
        when(renewalRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(renewal));
        when(sessionRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(session));
        when(agreementService.requireAcceptedForRenewal(renewal)).thenReturn(agreement);
        when(packageRepository.findById(101L)).thenReturn(Optional.of(currentPackage()));
        stubCapacity(eligibleProfile(2), 0, 0, 0);
        when(extensionRepository.existsByRenewalId(20L)).thenReturn(false);

        service.applyVerifiedPayment(payment, Instant.now());

        ArgumentCaptor<SessionExtension> extension = ArgumentCaptor.forClass(SessionExtension.class);
        verify(extensionRepository).saveAndFlush(extension.capture());
        assertEquals(renewal.getPreviousEndsAt(), extension.getValue().getPreviousEndsAt());
        assertEquals(renewal.getProposedNewEndsAt(), extension.getValue().getNewEndsAt());
        assertEquals(renewal.getProposedNewEndsAt(), session.getEndsAt());
        assertEquals(ConsultationRenewalStatus.APPLIED, renewal.getStatus());
        assertEquals(ConsultationPaymentStatus.PAID, payment.getStatus());
        verify(agreementService).consume(agreement);
        verify(operationalEventService).record(argThat(command ->
                command.eventType() == fit.iuh.se.hsoperations.entity.enums.BusinessEventType.RENEWAL_EXTENSION_APPLIED
                        && renewal.getPreviousEndsAt().toString().equals(command.metadata().get("previousEndsAt"))
                        && renewal.getProposedNewEndsAt().toString().equals(command.metadata().get("newEndsAt"))));
    }

    @Test
    void expiredOrFailedPaymentLeavesCurrentSessionUnchanged() {
        ConsultationSession session = session(ConsultationStatus.ACTIVE);
        ConsultationRenewal renewal = waitingPaymentRenewal(session);
        ConsultationPayment payment = renewalPayment();
        Instant originalEnd = session.getEndsAt();
        when(renewalRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(renewal));

        service.expireForPayment(payment, Instant.now());

        assertEquals(ConsultationRenewalStatus.EXPIRED, renewal.getStatus());
        assertEquals(originalEnd, session.getEndsAt());
        verify(extensionRepository, never()).saveAndFlush(any());
    }

    @Test
    void latePaymentAfterCompletionRequiresReviewAndNeverReopensSession() {
        ConsultationSession session = session(ConsultationStatus.COMPLETED);
        ConsultationRenewal renewal = waitingPaymentRenewal(session);
        ConsultationPayment payment = renewalPayment();
        Instant completedEnd = session.getEndsAt();
        when(renewalRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(renewal));
        when(sessionRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(session));

        service.applyVerifiedPayment(payment, Instant.now());

        assertEquals(ConsultationRenewalStatus.REQUIRES_REVIEW, renewal.getStatus());
        assertEquals(ConsultationPaymentStatus.REQUIRES_REVIEW, payment.getStatus());
        assertEquals(ConsultationStatus.COMPLETED, session.getStatus());
        assertEquals(completedEnd, session.getEndsAt());
        verify(extensionRepository, never()).saveAndFlush(any());
    }

    @Test
    void sequentialExtensionHistoryRemainsOnOneSessionInAppliedOrder() {
        ConsultationSession session = session(ConsultationStatus.ACTIVE);
        Instant firstEnd = session.getEndsAt();
        SessionExtension first = extension(31L, firstEnd, firstEnd.plus(30, ChronoUnit.DAYS), firstEnd);
        SessionExtension second = extension(32L, first.getNewEndsAt(),
                first.getNewEndsAt().plus(30, ChronoUnit.DAYS), firstEnd.plusSeconds(1));
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(extensionRepository.findBySessionIdOrderByAppliedAtAsc(10L)).thenReturn(List.of(first, second));

        var history = service.getMemberSessionExtensions(1L, 10L);

        assertEquals(2, history.size());
        assertEquals(firstEnd, history.getFirst().getPreviousEndsAt());
        assertEquals(first.getNewEndsAt(), history.getLast().getPreviousEndsAt());
        assertEquals(10L, history.getFirst().getSessionId());
        assertEquals(10L, history.getLast().getSessionId());
    }

    private void stubCapacity(DoctorCareProfile profile, long sessions, long reservations, long holds) {
        when(userAccountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(UserAccount.builder()
                .id(2L).email("doctor@test").passwordHash("x").role(UserRole.DOCTOR)
                .status(AccountStatus.ACTIVE).build()));
        when(profileRepository.findByDoctorId(2L)).thenReturn(Optional.of(profile));
        when(scheduleValidator.isValid(anyString(), anyString(), eq(true))).thenReturn(true);
        when(sessionRepository.countByDoctorIdAndIdNotAndStatusInAndStartedAtLessThanAndEndsAtGreaterThan(
                eq(2L), eq(10L), anyCollection(), any(), any())).thenReturn(sessions);
        when(reservationRepository.countByDoctorIdAndStatusAndExpiresAtAfter(eq(2L), any(), any()))
                .thenReturn(reservations);
        when(renewalRepository.countByDoctorIdAndSessionIdNotAndStatusInAndPreviousEndsAtLessThanAndProposedNewEndsAtGreaterThan(
                eq(2L), eq(10L), anyCollection(), any(), any())).thenReturn(holds);
    }

    private ConsultationSession session(ConsultationStatus status) {
        return ConsultationSession.builder().id(10L).memberId(1L).doctorId(2L).packageId(100L)
                .packageVersion(1).status(status).startedAt(Instant.now().minus(20, ChronoUnit.DAYS))
                .endsAt(Instant.now().plus(2, ChronoUnit.DAYS)).supportEndsAt(Instant.now().plus(2, ChronoUnit.DAYS))
                .build();
    }

    private CareServicePackage currentPackage() {
        return CareServicePackage.builder().id(101L).familyId(50L).code("CARE").versionNumber(2)
                .name("Care V2").description("Current terms").priceAmount(new BigDecimal("150000"))
                .currency("VND").durationDays(30).includedServices(List.of(CareServiceCode.SECURE_MESSAGING))
                .excludedServices(List.of()).supportPolicy(CareServiceSupportPolicy.ASSIGNED_DOCTOR_SUPPORT_SCHEDULE)
                .renewable(true).status(CareServicePackageStatus.ACTIVE).build();
    }

    private DoctorCareProfile eligibleProfile(int capacity) {
        return DoctorCareProfile.builder().id(3L).doctorId(2L).acceptsOneOnOneCare(true)
                .maxActiveConsultations(capacity).specialty(DoctorSpecialty.GENERAL_PRACTICE)
                .availabilityJson("{\"monday\":[]}").timezone("Asia/Ho_Chi_Minh").build();
    }

    private ConsultationRenewal requestedRenewal() {
        return ConsultationRenewal.builder().id(20L).sessionId(10L).memberId(1L).doctorId(2L)
                .packageFamilyId(50L).status(ConsultationRenewalStatus.UNDER_REVIEW)
                .requestedAt(Instant.now()).build();
    }

    private ConsultationRenewal waitingPaymentRenewal(ConsultationSession session) {
        Instant oldEnd = session.getEndsAt();
        return ConsultationRenewal.builder().id(20L).sessionId(10L).memberId(1L).doctorId(2L)
                .packageFamilyId(50L).packageId(101L).packageVersion(2).durationDays(30)
                .priceAmount(new BigDecimal("150000")).currency("VND")
                .supportScheduleSnapshotJson("{\"monday\":[]}").supportTimezoneSnapshot("Asia/Ho_Chi_Minh")
                .previousEndsAt(oldEnd).proposedNewEndsAt(oldEnd.plus(30, ChronoUnit.DAYS))
                .agreementId(40L).status(ConsultationRenewalStatus.WAITING_PAYMENT)
                .requestedAt(Instant.now()).paymentDeadline(Instant.now().plusSeconds(600)).build();
    }

    private CareServiceAgreement renewalAgreement() {
        return CareServiceAgreement.builder().id(40L).renewalId(20L)
                .agreementType(CareServiceAgreementType.RENEWAL).build();
    }

    private CareServiceAgreement acceptedRenewalAgreement(ConsultationRenewal renewal) {
        return CareServiceAgreement.builder().id(40L).renewalId(20L).memberId(1L).doctorId(2L)
                .agreementType(CareServiceAgreementType.RENEWAL).status(CareServiceAgreementStatus.ACCEPTED)
                .priceAmount(new BigDecimal("150000")).currency("VND")
                .resultingEndsAt(renewal.getProposedNewEndsAt())
                .validUntil(Instant.now().plusSeconds(600)).build();
    }

    private ConsultationPayment renewalPayment() {
        return ConsultationPayment.builder().id(30L).renewalId(20L)
                .paymentPurpose(ConsultationPaymentPurpose.RENEWAL).agreementId(40L).attemptNumber(1)
                .memberId(1L).amount(new BigDecimal("150000")).currency("VND")
                .status(ConsultationPaymentStatus.PENDING).build();
    }

    private SessionExtension extension(Long id, Instant oldEnd, Instant newEnd, Instant appliedAt) {
        return SessionExtension.builder().id(id).sessionId(10L).renewalId(id + 100)
                .agreementId(id + 200).paymentId(id + 300).previousEndsAt(oldEnd).newEndsAt(newEnd)
                .durationDays(30).packageId(101L).packageVersion(2)
                .priceAmount(new BigDecimal("150000")).currency("VND")
                .supportScheduleSnapshotJson("{}").supportTimezoneSnapshot("Asia/Ho_Chi_Minh")
                .appliedAt(appliedAt).build();
    }
}
