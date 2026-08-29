package fit.iuh.se.hschat.service.session.impl;

import fit.iuh.se.hschat.dto.request.AdminCreateConsultationSessionRequest;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationCompletionReason;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.CareServicePackageRepository;
import fit.iuh.se.hschat.repository.ConsultationMessageRepository;
import fit.iuh.se.hschat.repository.ConsultationParticipantRepository;
import fit.iuh.se.hschat.repository.ConsultationRequestRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.DoctorCareProfileRepository;
import fit.iuh.se.hschat.service.doctor.SupportScheduleValidator;
import fit.iuh.se.hschat.service.authorization.EpisodeHealthRecordAuthorizationService;
import fit.iuh.se.hschat.service.reservation.DoctorReservationService;
import fit.iuh.se.hschat.service.finalsummary.FinalSummaryClosureService;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationSessionServiceImplTest {

    @Mock
    ConsultationSessionRepository sessionRepository;
    @Mock
    ConsultationParticipantRepository participantRepository;
    @Mock
    ConsultationMessageRepository messageRepository;
    @Mock
    HealthRecordRepository healthRecordRepository;
    @Mock
    UserAccountRepository userAccountRepository;
    @Mock
    ConsultationRequestRepository requestRepository;
    @Mock
    CareServicePackageRepository packageRepository;
    @Mock
    DoctorCareProfileRepository doctorCareProfileRepository;
    @Mock
    SupportScheduleValidator scheduleValidator;
    @Mock
    DoctorReservationService reservationService;
    @Mock
    EpisodeHealthRecordAuthorizationService authorizationService;
    @Mock
    FinalSummaryClosureService finalSummaryClosureService;
    @Mock
    ConsultationMapper mapper;

    ConsultationSessionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConsultationSessionServiceImpl(
                sessionRepository,
                participantRepository,
                messageRepository,
                healthRecordRepository,
                userAccountRepository,
                requestRepository,
                packageRepository,
                doctorCareProfileRepository,
                scheduleValidator,
                reservationService,
                authorizationService,
                finalSummaryClosureService,
                mapper
        );
        ReflectionTestUtils.setField(service, "defaultDoctorMaxActiveSessions", 5);
    }

    @Test
    void expireOverdueSessionsCompletesActiveSessions() {
        ConsultationSession overdue = ConsultationSession.builder()
                .id(1L)
                .status(ConsultationStatus.ACTIVE)
                .endsAt(Instant.now().minusSeconds(60))
                .build();
        when(sessionRepository.findByStatusAndEndsAtBefore(eq(ConsultationStatus.ACTIVE), any(Instant.class)))
                .thenReturn(List.of(overdue));
        when(sessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(overdue));

        service.expireOverdueSessions(UserRole.CARE_COORDINATOR);

        ArgumentCaptor<ConsultationSession> captor = ArgumentCaptor.forClass(ConsultationSession.class);
        verify(sessionRepository).save(captor.capture());
        ConsultationSession saved = captor.getValue();
        assertEquals(ConsultationStatus.COMPLETED, saved.getStatus());
        assertEquals(ConsultationCompletionReason.PERIOD_ENDED, saved.getCompletionReason());
        assertNotNull(saved.getCompletedAt());
        verify(finalSummaryClosureService).onSessionCompleted(eq(overdue), any(Instant.class));
        verify(finalSummaryClosureService).refreshOpenClosures(any(Instant.class));
    }

    @Test
    void successfulRenewalDefersCompletionAndFinalSummaryUntilFinalEffectiveEnd() {
        ConsultationSession staleCandidate = ConsultationSession.builder().id(1L)
                .status(ConsultationStatus.ACTIVE).endsAt(Instant.now().minusSeconds(60)).build();
        ConsultationSession renewedLockedSession = ConsultationSession.builder().id(1L)
                .status(ConsultationStatus.ACTIVE).endsAt(Instant.now().plusSeconds(86400)).build();
        when(sessionRepository.findByStatusAndEndsAtBefore(eq(ConsultationStatus.ACTIVE), any()))
                .thenReturn(List.of(staleCandidate));
        when(sessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(renewedLockedSession));

        service.expireOverdueSessions(UserRole.CARE_COORDINATOR);

        assertEquals(ConsultationStatus.ACTIVE, renewedLockedSession.getStatus());
        verify(sessionRepository, never()).save(any());
        verify(finalSummaryClosureService, never()).onSessionCompleted(any(), any());
        verify(finalSummaryClosureService).refreshOpenClosures(any());
    }

    @Test
    void activateScheduledSessionsMovesDueSessionsToActive() {
        ConsultationSession scheduled = ConsultationSession.builder()
                .id(1L)
                .status(ConsultationStatus.SCHEDULED)
                .startedAt(Instant.now().minusSeconds(60))
                .build();
        when(sessionRepository.findByStatusAndStartedAtBefore(eq(ConsultationStatus.SCHEDULED), any(Instant.class)))
                .thenReturn(List.of(scheduled));

        service.activateScheduledSessions(UserRole.CARE_COORDINATOR);

        ArgumentCaptor<ConsultationSession> captor = ArgumentCaptor.forClass(ConsultationSession.class);
        verify(sessionRepository).save(captor.capture());
        assertEquals(ConsultationStatus.ACTIVE, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getActivatedAt());
    }

    @Test
    void scheduledDoctorSessionResponseDoesNotRevealHealthRecordReferenceBeforeActivation() {
        ConsultationSession scheduled = ConsultationSession.builder()
                .id(1L)
                .memberId(1L)
                .doctorId(2L)
                .status(ConsultationStatus.SCHEDULED)
                .healthRecordId(500L)
                .build();
        var response = fit.iuh.se.hschat.dto.response.ConsultationSessionResponse.builder()
                .healthRecordId(500L)
                .build();
        when(participantRepository.existsBySessionIdAndUserIdAndActiveTrue(1L, 2L)).thenReturn(true);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(scheduled));
        when(mapper.toSessionResponse(scheduled)).thenReturn(response);
        when(participantRepository.findBySessionIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertNull(service.getSessionById(2L, 1L).getHealthRecordId());
    }

    @Test
    void createSessionByAdminRejectsWhenMemberAlreadyHasScheduledSession() {
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(userAccountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(user(2L, UserRole.DOCTOR)));
        when(doctorCareProfileRepository.findByDoctorId(2L)).thenReturn(Optional.of(doctorProfile()));
        when(scheduleValidator.isValid(anyString(), anyString(), eq(true))).thenReturn(true);
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(true);

        AdminCreateConsultationSessionRequest request = new AdminCreateConsultationSessionRequest();
        request.setMemberId(1L);
        request.setDoctorId(2L);
        request.setEndsAt(Instant.now().plusSeconds(3600));
        request.setOverrideReason("Administrative recovery");

        assertThrows(AppException.class, () -> service.createSessionByAdmin(9L, UserRole.ADMIN, request));
        verify(sessionRepository, never()).save(any());
        verify(participantRepository, never()).save(any());
    }

    @Test
    void createSessionByAdminSnapshotsDoctorSupportSchedule() {
        DoctorCareProfile profile = doctorProfile();
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(userAccountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(user(2L, UserRole.DOCTOR)));
        when(doctorCareProfileRepository.findByDoctorId(2L)).thenReturn(Optional.of(profile));
        when(scheduleValidator.isValid(anyString(), anyString(), eq(true))).thenReturn(true);
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(reservationService.getEffectiveLoad(eq(2L), any(Instant.class))).thenReturn(0L);
        when(sessionRepository.save(any(ConsultationSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminCreateConsultationSessionRequest request = new AdminCreateConsultationSessionRequest();
        request.setMemberId(1L);
        request.setDoctorId(2L);
        request.setStartedAt(Instant.now().minusSeconds(60));
        request.setEndsAt(Instant.now().plusSeconds(3600));
        request.setOverrideReason("Compensating care approved by Admin");

        service.createSessionByAdmin(9L, UserRole.ADMIN, request);

        ArgumentCaptor<ConsultationSession> captor = ArgumentCaptor.forClass(ConsultationSession.class);
        verify(sessionRepository).save(captor.capture());
        ConsultationSession saved = captor.getValue();
        assertEquals(profile.getAvailabilityJson(), saved.getSupportScheduleSnapshotJson());
        assertEquals(profile.getTimezone(), saved.getSupportTimezoneSnapshot());
        assertTrue(saved.getExceptionalOverride());
        assertEquals("Compensating care approved by Admin", saved.getOverrideReason());
    }

    @Test
    void coordinatorCannotUseExceptionalOverride() {
        AdminCreateConsultationSessionRequest request = new AdminCreateConsultationSessionRequest();
        request.setOverrideReason("Attempted bypass");

        assertThrows(AppException.class,
                () -> service.createSessionByAdmin(9L, UserRole.CARE_COORDINATOR, request));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void adminOverrideRequiresExplicitReason() {
        AdminCreateConsultationSessionRequest request = new AdminCreateConsultationSessionRequest();

        assertThrows(AppException.class,
                () -> service.createSessionByAdmin(9L, UserRole.ADMIN, request));
        verify(sessionRepository, never()).save(any());
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

    private DoctorCareProfile doctorProfile() {
        return DoctorCareProfile.builder()
                .doctorId(2L)
                .specialty(DoctorSpecialty.CARDIOLOGY)
                .acceptsOneOnOneCare(true)
                .maxActiveConsultations(5)
                .availabilityJson("{\"weekly\":[{\"dayOfWeek\":\"MONDAY\",\"start\":\"07:00\",\"end\":\"11:00\"}]}")
                .timezone("Asia/Ho_Chi_Minh")
                .build();
    }
}
