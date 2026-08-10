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

        service.expireOverdueSessions(UserRole.CARE_COORDINATOR);

        ArgumentCaptor<ConsultationSession> captor = ArgumentCaptor.forClass(ConsultationSession.class);
        verify(sessionRepository).save(captor.capture());
        ConsultationSession saved = captor.getValue();
        assertEquals(ConsultationStatus.COMPLETED, saved.getStatus());
        assertEquals(ConsultationCompletionReason.PERIOD_ENDED, saved.getCompletionReason());
        assertNotNull(saved.getCompletedAt());
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
    }

    @Test
    void createSessionByAdminRejectsWhenMemberAlreadyHasScheduledSession() {
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(user(2L, UserRole.DOCTOR)));
        when(doctorCareProfileRepository.findByDoctorId(2L)).thenReturn(Optional.of(doctorProfile()));
        when(scheduleValidator.isValid(anyString(), anyString(), eq(true))).thenReturn(true);
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(true);

        AdminCreateConsultationSessionRequest request = new AdminCreateConsultationSessionRequest();
        request.setMemberId(1L);
        request.setDoctorId(2L);
        request.setEndsAt(Instant.now().plusSeconds(3600));

        assertThrows(AppException.class, () -> service.createSessionByAdmin(9L, UserRole.ADMIN, request));
        verify(sessionRepository, never()).save(any());
        verify(participantRepository, never()).save(any());
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
