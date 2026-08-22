package fit.iuh.se.hschat.service.activecare.impl;

import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.ConsultationHealthRecordAttentionRepository;
import fit.iuh.se.hschat.repository.ConsultationMessageRepository;
import fit.iuh.se.hschat.repository.ConsultationParticipantRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.mapper.HealthRecordMapper;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorActiveCareServiceImplTest {

    @Mock
    ConsultationSessionRepository sessionRepository;
    @Mock
    ConsultationParticipantRepository participantRepository;
    @Mock
    ConsultationMessageRepository messageRepository;
    @Mock
    ConsultationHealthRecordAttentionRepository attentionRepository;
    @Mock
    HealthRecordRepository healthRecordRepository;
    @Mock
    UserAccountRepository userAccountRepository;
    @Mock
    ConsultationMapper consultationMapper;
    @Mock
    HealthRecordMapper healthRecordMapper;

    DoctorActiveCareServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DoctorActiveCareServiceImpl(
                sessionRepository,
                participantRepository,
                messageRepository,
                attentionRepository,
                healthRecordRepository,
                userAccountRepository,
                consultationMapper,
                healthRecordMapper
        );
    }

    @Test
    void attachedInitialRecordIsVisibleEvenIfCreatedBeforeSessionStart() {
        ConsultationSession session = session(200L);
        HealthRecord record = record(200L, Instant.parse("2026-08-01T08:00:00Z"));
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session));
        when(healthRecordRepository.findByIdAndUserId(200L, 1L)).thenReturn(Optional.of(record));
        when(healthRecordMapper.toResponse(record)).thenReturn(HealthRecordResponse.builder().id(200L).build());

        service.getScopedHealthRecord(2L, 100L, 200L);

        verify(healthRecordMapper).toResponse(record);
    }

    @Test
    void outOfWindowRecordIsDeniedWhenNotInitialAttachedRecord() {
        ConsultationSession session = session(null);
        HealthRecord record = record(201L, Instant.parse("2026-08-20T08:00:00Z"));
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session));
        when(healthRecordRepository.findByIdAndUserId(201L, 1L)).thenReturn(Optional.of(record));

        assertThrows(AppException.class, () -> service.getScopedHealthRecord(2L, 100L, 201L));
        verify(healthRecordMapper, never()).toResponse(any());
    }

    @Test
    void nonAssignedDoctorCannotMarkAttentionReviewed() {
        when(sessionRepository.findByIdAndDoctorId(100L, 9L)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> service.markAttentionReviewed(9L, 100L, 200L));
        verify(attentionRepository, never()).save(any());
    }

    private ConsultationSession session(Long healthRecordId) {
        return ConsultationSession.builder()
                .id(100L)
                .memberId(1L)
                .doctorId(2L)
                .status(ConsultationStatus.ACTIVE)
                .startedAt(Instant.parse("2026-08-10T08:00:00Z"))
                .endsAt(Instant.parse("2026-08-17T08:00:00Z"))
                .healthRecordId(healthRecordId)
                .build();
    }

    private HealthRecord record(Long id, Instant createdAt) {
        HealthRecord record = HealthRecord.builder()
                .id(id)
                .userId(1L)
                .fileName("record.csv")
                .s3FileKey("records/record.csv")
                .build();
        record.setCreatedAt(createdAt);
        return record;
    }
}
