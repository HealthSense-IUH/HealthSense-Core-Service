package fit.iuh.se.hschat.service.authorization.impl;

import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.EpisodeHealthRecordAuthorization;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.EpisodeHealthRecordAuthorizationRepository;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EpisodeHealthRecordAuthorizationServiceImplTest {

    @Mock EpisodeHealthRecordAuthorizationRepository authorizationRepository;
    @Mock ConsultationSessionRepository sessionRepository;
    @Mock HealthRecordRepository healthRecordRepository;

    EpisodeHealthRecordAuthorizationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EpisodeHealthRecordAuthorizationServiceImpl(
                authorizationRepository, sessionRepository, healthRecordRepository);
        lenient().when(authorizationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void multipleInitialIntakeRecordsBecomeDurableAuthorizationsAtActivation() {
        ConsultationSession session = session(100L, 1L, 2L, ConsultationStatus.ACTIVE, Instant.now());
        allowOwnedRecord(501L, 1L);
        allowOwnedRecord(502L, 1L);

        var result = service.authorizeInitialRecords(session, List.of(501L, 502L, 501L));

        assertEquals(2, result.size());
        verify(authorizationRepository, times(2)).save(argThat(item ->
                item.getAuthorizationSource() == EpisodeHealthRecordAuthorizationSource.INITIAL_SHARED
                        && item.getAuthorizedByType() == EpisodeHealthRecordAuthorizedByType.SYSTEM));
    }

    @Test
    void memberCanShareOwnHistoricalRecordDuringActiveCare() {
        ConsultationSession session = session(100L, 1L, 2L, ConsultationStatus.ACTIVE, Instant.now());
        when(sessionRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(session));
        allowOwnedRecord(503L, 1L);

        var result = service.shareDuringActiveCare(1L, 100L, 503L);

        assertEquals(EpisodeHealthRecordAuthorizationSource.SHARED_DURING_CARE, result.getSource());
        assertEquals(EpisodeHealthRecordAuthorizedByType.MEMBER, result.getAuthorizedByType());
    }

    @Test
    void memberCannotShareAnotherMembersRecord() {
        ConsultationSession session = session(100L, 1L, 2L, ConsultationStatus.ACTIVE, Instant.now());
        when(sessionRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(session));
        when(healthRecordRepository.findByIdAndUserId(503L, 1L)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> service.shareDuringActiveCare(1L, 100L, 503L));
        verify(authorizationRepository, never()).save(any());
    }

    @Test
    void newRecordBusinessEventCreatesDurableAssociationForActiveEpisode() {
        ConsultationSession session = session(100L, 1L, 2L, ConsultationStatus.ACTIVE, Instant.now());
        allowOwnedRecord(504L, 1L);
        when(sessionRepository.findAllByMemberIdAndStatus(1L, ConsultationStatus.ACTIVE))
                .thenReturn(List.of(session));
        when(sessionRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(session));

        service.authorizeCreatedRecord(1L, 504L);

        verify(authorizationRepository).save(argThat(item ->
                item.getAuthorizationSource() == EpisodeHealthRecordAuthorizationSource.CREATED_DURING_CARE));
    }

    @Test
    void doctorRequestOrTimestampAloneDoesNotGrantAccess() {
        ConsultationSession session = session(100L, 1L, 2L, ConsultationStatus.ACTIVE, Instant.now());
        when(authorizationRepository.findBySessionIdAndHealthRecordId(100L, 505L))
                .thenReturn(Optional.empty());

        assertThrows(AppException.class,
                () -> service.requireDoctorReadAccess(2L, session, 505L));
    }

    @Test
    void previousDoctorReadsOnlyOwnActivatedEpisodeRecordAndCannotWrite() {
        ConsultationSession previous = session(100L, 1L, 2L, ConsultationStatus.COMPLETED, Instant.now());
        EpisodeHealthRecordAuthorization relation = relation(100L, 501L);
        when(authorizationRepository.findBySessionIdAndHealthRecordId(100L, 501L))
                .thenReturn(Optional.of(relation));

        assertSame(relation, service.requireDoctorReadAccess(2L, previous, 501L));
        assertThrows(AppException.class,
                () -> service.requireDoctorCurrentWriteAccess(2L, previous, 501L));
        assertThrows(AppException.class,
                () -> service.requireDoctorReadAccess(9L, previous, 501L));
    }

    @Test
    void previousDoctorCannotReadLaterMemberRecordNotInOwnEpisode() {
        ConsultationSession previous = session(100L, 1L, 2L, ConsultationStatus.COMPLETED, Instant.now());
        when(authorizationRepository.findBySessionIdAndHealthRecordId(100L, 999L))
                .thenReturn(Optional.empty());

        assertThrows(AppException.class,
                () -> service.requireDoctorReadAccess(2L, previous, 999L));
    }

    @Test
    void cancelledAfterActivationRetainsReadOnlyRelationButCancelledBeforeActivationDoesNot() {
        ConsultationSession after = session(100L, 1L, 2L, ConsultationStatus.CANCELLED, Instant.now());
        ConsultationSession before = session(101L, 1L, 2L, ConsultationStatus.CANCELLED, null);
        when(authorizationRepository.findBySessionIdAndHealthRecordId(100L, 501L))
                .thenReturn(Optional.of(relation(100L, 501L)));

        assertNotNull(service.requireDoctorReadAccess(2L, after, 501L));
        assertThrows(AppException.class,
                () -> service.requireDoctorCurrentWriteAccess(2L, after, 501L));
        assertThrows(AppException.class,
                () -> service.requireDoctorReadAccess(2L, before, 501L));
    }

    private void allowOwnedRecord(Long recordId, Long memberId) {
        when(healthRecordRepository.findByIdAndUserId(recordId, memberId))
                .thenReturn(Optional.of(HealthRecord.builder().id(recordId).userId(memberId).build()));
        when(authorizationRepository.findBySessionIdAndHealthRecordId(anyLong(), eq(recordId)))
                .thenReturn(Optional.empty());
    }

    private ConsultationSession session(Long id, Long memberId, Long doctorId,
                                        ConsultationStatus status, Instant activatedAt) {
        return ConsultationSession.builder()
                .id(id).memberId(memberId).doctorId(doctorId).status(status)
                .activatedAt(activatedAt).endsAt(Instant.now().plusSeconds(3600)).build();
    }

    private EpisodeHealthRecordAuthorization relation(Long sessionId, Long recordId) {
        return EpisodeHealthRecordAuthorization.builder()
                .sessionId(sessionId).healthRecordId(recordId).memberId(1L)
                .authorizationSource(EpisodeHealthRecordAuthorizationSource.INITIAL_SHARED)
                .build();
    }
}
