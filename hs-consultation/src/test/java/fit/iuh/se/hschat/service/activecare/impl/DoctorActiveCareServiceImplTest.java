package fit.iuh.se.hschat.service.activecare.impl;

import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.EpisodeHealthRecordAuthorization;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.entity.enums.EpisodeHealthRecordAuthorizationSource;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.*;
import fit.iuh.se.hschat.service.authorization.EpisodeHealthRecordAuthorizationService;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.mapper.HealthRecordMapper;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.service.s3.S3Service;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import fit.iuh.se.hsuser.entity.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorActiveCareServiceImplTest {

    @Mock ConsultationSessionRepository sessionRepository;
    @Mock ConsultationParticipantRepository participantRepository;
    @Mock ConsultationMessageRepository messageRepository;
    @Mock ConsultationHealthRecordAttentionRepository attentionRepository;
    @Mock HealthRecordRepository healthRecordRepository;
    @Mock EpisodeHealthRecordAuthorizationService authorizationService;
    @Mock UserAccountRepository userAccountRepository;
    @Mock ConsultationMapper consultationMapper;
    @Mock HealthRecordMapper healthRecordMapper;
    @Mock S3Service s3Service;
    @Mock fit.iuh.se.hsoperations.service.OperationalEventService operationalEventService;

    DoctorActiveCareServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DoctorActiveCareServiceImpl(
                sessionRepository, participantRepository, messageRepository, attentionRepository,
                healthRecordRepository, authorizationService, userAccountRepository,
                consultationMapper, healthRecordMapper, s3Service, operationalEventService
        );
        lenient().when(userAccountRepository.findById(2L)).thenReturn(Optional.of(
                UserAccount.builder().id(2L).status(fit.iuh.se.hsuser.entity.enums.AccountStatus.ACTIVE).build()));
    }

    @Test
    void selectedRecordCannotBeReadBeforeSessionActivation() {
        ConsultationSession session = session(ConsultationStatus.SCHEDULED, null);
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session));

        assertThrows(AppException.class, () -> service.getScopedHealthRecord(2L, 100L, 200L));

        verifyNoInteractions(healthRecordRepository, authorizationService, healthRecordMapper);
    }

    @Test
    void explicitlyAuthorizedRecordAndItsAiResultAreVisibleRegardlessOfCreationTime() {
        ConsultationSession session = session(ConsultationStatus.ACTIVE, Instant.now());
        HealthRecord record = record(200L);
        EpisodeHealthRecordAuthorization authorization = authorization(100L, 200L);
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session));
        when(healthRecordRepository.findByIdAndUserId(200L, 1L)).thenReturn(Optional.of(record));
        when(authorizationService.requireDoctorReadAccess(2L, session, 200L)).thenReturn(authorization);
        when(healthRecordMapper.toResponse(record)).thenReturn(HealthRecordResponse.builder().id(200L).build());

        var response = service.getScopedHealthRecord(2L, 100L, 200L);

        assertEquals(200L, response.getRecord().getId());
        assertEquals(EpisodeHealthRecordAuthorizationSource.INITIAL_SHARED, response.getAuthorizationSource());
    }

    @Test
    void aiResultIsDeniedWhenParentRecordIsNotAuthorized() {
        ConsultationSession session = session(ConsultationStatus.ACTIVE, Instant.now());
        HealthRecord record = record(201L);
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session));
        when(healthRecordRepository.findByIdAndUserId(201L, 1L)).thenReturn(Optional.of(record));
        when(authorizationService.requireDoctorReadAccess(2L, session, 201L))
                .thenThrow(new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));

        assertThrows(AppException.class, () -> service.getScopedHealthRecord(2L, 100L, 201L));
        verify(healthRecordMapper, never()).toResponse(any());
    }

    @Test
    void rawArtifactCannotBypassEpisodeAuthorization() {
        ConsultationSession session = session(ConsultationStatus.ACTIVE, Instant.now());
        HealthRecord record = record(201L);
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session));
        when(healthRecordRepository.findByIdAndUserId(201L, 1L)).thenReturn(Optional.of(record));
        when(authorizationService.requireDoctorReadAccess(2L, session, 201L))
                .thenThrow(new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));

        assertThrows(AppException.class, () -> service.getRawArtifact(2L, 100L, 201L));
        verifyNoInteractions(s3Service);
    }

    @Test
    void assignedDoctorCanReadOwnCancelledAfterActivationEpisodeButNotMutateIt() {
        ConsultationSession session = session(ConsultationStatus.CANCELLED, Instant.now());
        HealthRecord record = record(200L);
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session));
        when(healthRecordRepository.findByIdAndUserId(200L, 1L)).thenReturn(Optional.of(record));
        when(authorizationService.requireDoctorReadAccess(2L, session, 200L))
                .thenReturn(authorization(100L, 200L));
        when(healthRecordMapper.toResponse(record)).thenReturn(HealthRecordResponse.builder().id(200L).build());

        assertNotNull(service.getScopedHealthRecord(2L, 100L, 200L));
        verify(operationalEventService).record(argThat(command ->
                command.eventType() == fit.iuh.se.hsoperations.entity.enums.BusinessEventType.HEALTH_RECORD_HISTORICAL_ACCESSED
                        && Long.valueOf(100L).equals(command.sessionId())
                        && Long.valueOf(200L).equals(command.healthRecordId())
                        && Long.valueOf(2L).equals(command.actorUserId())));
        when(authorizationService.requireDoctorCurrentWriteAccess(2L, session, 200L))
                .thenThrow(new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
        assertThrows(AppException.class, () -> service.markAttentionReviewed(2L, 100L, 200L));
    }

    @Test
    void historicalRecordListAccessIsAuditedForEachReturnedRecord() {
        ConsultationSession session = session(ConsultationStatus.COMPLETED, Instant.now());
        HealthRecord record = record(200L);
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session));
        when(authorizationService.getSessionAuthorizations(100L))
                .thenReturn(List.of(authorization(100L, 200L)));
        when(healthRecordRepository.findAllById(List.of(200L))).thenReturn(List.of(record));
        when(authorizationService.requireDoctorReadAccess(2L, session, 200L))
                .thenReturn(authorization(100L, 200L));
        when(healthRecordMapper.toResponse(record)).thenReturn(HealthRecordResponse.builder().id(200L).build());

        var result = service.getScopedHealthRecords(2L, 100L, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        verify(operationalEventService).record(argThat(command ->
                command.eventType() == fit.iuh.se.hsoperations.entity.enums.BusinessEventType.HEALTH_RECORD_HISTORICAL_ACCESSED
                        && Long.valueOf(200L).equals(command.healthRecordId())));
    }

    @Test
    void cancelledBeforeActivationCreatesNoHistoricalDoctorAccess() {
        ConsultationSession session = session(ConsultationStatus.CANCELLED, null);
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session));

        assertThrows(AppException.class, () -> service.getScopedHealthRecord(2L, 100L, 200L));
        verifyNoInteractions(authorizationService);
    }

    @Test
    void doctorBCannotOpenDoctorAEpisode() {
        when(sessionRepository.findByIdAndDoctorId(100L, 9L)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> service.getRawArtifact(9L, 100L, 200L));
        verifyNoInteractions(authorizationService, s3Service);
    }

    private ConsultationSession session(ConsultationStatus status, Instant activatedAt) {
        return ConsultationSession.builder()
                .id(100L).memberId(1L).doctorId(2L).status(status)
                .startedAt(Instant.parse("2026-08-10T08:00:00Z"))
                .activatedAt(activatedAt)
                .endsAt(Instant.parse("2026-08-17T08:00:00Z"))
                .healthRecordId(200L)
                .build();
    }

    private HealthRecord record(Long id) {
        return HealthRecord.builder()
                .id(id).userId(1L).fileName("record.csv").s3FileKey("records/record.csv")
                .build();
    }

    private EpisodeHealthRecordAuthorization authorization(Long sessionId, Long recordId) {
        return EpisodeHealthRecordAuthorization.builder()
                .sessionId(sessionId).healthRecordId(recordId).memberId(1L)
                .authorizationSource(EpisodeHealthRecordAuthorizationSource.INITIAL_SHARED)
                .build();
    }
}
