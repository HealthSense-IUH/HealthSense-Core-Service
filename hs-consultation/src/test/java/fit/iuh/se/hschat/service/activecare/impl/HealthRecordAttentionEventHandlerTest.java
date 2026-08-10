package fit.iuh.se.hschat.service.activecare.impl;

import fit.iuh.se.hschat.entity.ConsultationHealthRecordAttention;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.repository.ConsultationHealthRecordAttentionRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.event.HealthRecordAnalyzedEvent;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthRecordAttentionEventHandlerTest {

    @Mock
    ConsultationSessionRepository sessionRepository;
    @Mock
    ConsultationHealthRecordAttentionRepository attentionRepository;
    @Mock
    HealthRecordRepository healthRecordRepository;

    HealthRecordAttentionEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new HealthRecordAttentionEventHandler(sessionRepository, attentionRepository, healthRecordRepository);
    }

    @Test
    void afibRecordCreatesAttentionForActiveInScopeSession() {
        HealthRecord record = record(200L, Instant.parse("2026-08-10T08:30:00Z"));
        ConsultationSession session = session(100L, ConsultationStatus.ACTIVE, null);
        when(healthRecordRepository.findByIdAndUserId(200L, 1L)).thenReturn(Optional.of(record));
        when(sessionRepository.findAllByMemberIdAndStatus(1L, ConsultationStatus.ACTIVE)).thenReturn(List.of(session));
        when(attentionRepository.existsBySessionIdAndHealthRecordId(100L, 200L)).thenReturn(false);

        handler.onHealthRecordAnalyzed(event(PredictionLabel.AFIB));

        verify(attentionRepository).save(any(ConsultationHealthRecordAttention.class));
    }

    @Test
    void initialAttachedRecordCreatesAttentionEvenIfBeforeSessionStart() {
        HealthRecord record = record(200L, Instant.parse("2026-08-01T08:30:00Z"));
        ConsultationSession session = session(100L, ConsultationStatus.ACTIVE, 200L);
        when(healthRecordRepository.findByIdAndUserId(200L, 1L)).thenReturn(Optional.of(record));
        when(sessionRepository.findAllByMemberIdAndStatus(1L, ConsultationStatus.ACTIVE)).thenReturn(List.of(session));
        when(attentionRepository.existsBySessionIdAndHealthRecordId(100L, 200L)).thenReturn(false);

        handler.onHealthRecordAnalyzed(event(PredictionLabel.AFIB));

        verify(attentionRepository).save(any(ConsultationHealthRecordAttention.class));
    }

    @Test
    void nonAfibRecordDoesNotCreateAttention() {
        handler.onHealthRecordAnalyzed(event(PredictionLabel.NORMAL));

        verifyNoInteractions(healthRecordRepository, sessionRepository, attentionRepository);
    }

    @Test
    void outOfScopeRecordDoesNotCreateAttention() {
        HealthRecord record = record(200L, Instant.parse("2026-08-20T08:30:00Z"));
        ConsultationSession session = session(100L, ConsultationStatus.ACTIVE, null);
        when(healthRecordRepository.findByIdAndUserId(200L, 1L)).thenReturn(Optional.of(record));
        when(sessionRepository.findAllByMemberIdAndStatus(1L, ConsultationStatus.ACTIVE)).thenReturn(List.of(session));

        handler.onHealthRecordAnalyzed(event(PredictionLabel.AFIB));

        verify(attentionRepository, never()).save(any());
    }

    @Test
    void duplicateEventDoesNotCreateDuplicateAttention() {
        HealthRecord record = record(200L, Instant.parse("2026-08-10T08:30:00Z"));
        ConsultationSession session = session(100L, ConsultationStatus.ACTIVE, null);
        when(healthRecordRepository.findByIdAndUserId(200L, 1L)).thenReturn(Optional.of(record));
        when(sessionRepository.findAllByMemberIdAndStatus(1L, ConsultationStatus.ACTIVE)).thenReturn(List.of(session));
        when(attentionRepository.existsBySessionIdAndHealthRecordId(100L, 200L)).thenReturn(true);

        handler.onHealthRecordAnalyzed(event(PredictionLabel.AFIB));

        verify(attentionRepository, never()).save(any());
    }

    private HealthRecordAnalyzedEvent event(PredictionLabel label) {
        return HealthRecordAnalyzedEvent.builder()
                .recordId(200L)
                .userId(1L)
                .predictionLabel(label)
                .confidence(0.95)
                .analyzedAt(Instant.now())
                .build();
    }

    private ConsultationSession session(Long id, ConsultationStatus status, Long healthRecordId) {
        ConsultationSession session = ConsultationSession.builder()
                .id(id)
                .memberId(1L)
                .doctorId(2L)
                .status(status)
                .startedAt(Instant.parse("2026-08-10T08:00:00Z"))
                .endsAt(Instant.parse("2026-08-17T08:00:00Z"))
                .healthRecordId(healthRecordId)
                .build();
        return session;
    }

    private HealthRecord record(Long id, Instant createdAt) {
        HealthRecord record = HealthRecord.builder()
                .id(id)
                .userId(1L)
                .fileName("record.csv")
                .s3FileKey("records/record.csv")
                .predictionLabel(PredictionLabel.AFIB)
                .build();
        record.setCreatedAt(createdAt);
        return record;
    }
}
