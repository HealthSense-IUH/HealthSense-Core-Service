package fit.iuh.se.hschat.service.activecare.impl;

import fit.iuh.se.hschat.entity.ConsultationHealthRecordAttention;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.repository.*;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthRecordAttentionEventHandlerTest {

    @Mock ConsultationSessionRepository sessionRepository;
    @Mock ConsultationHealthRecordAttentionRepository attentionRepository;
    @Mock HealthRecordRepository healthRecordRepository;
    @Mock EpisodeHealthRecordAuthorizationRepository authorizationRepository;

    HealthRecordAttentionEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new HealthRecordAttentionEventHandler(
                sessionRepository, attentionRepository, healthRecordRepository, authorizationRepository);
    }

    @Test
    void highRiskCreatesAttentionOnlyForExplicitlyAuthorizedActiveEpisode() {
        HealthRecord record = record();
        ConsultationSession session = session();
        when(healthRecordRepository.findByIdAndUserId(200L, 1L)).thenReturn(Optional.of(record));
        when(sessionRepository.findAllByMemberIdAndStatus(1L, ConsultationStatus.ACTIVE)).thenReturn(List.of(session));
        when(authorizationRepository.existsBySessionIdAndHealthRecordId(100L, 200L)).thenReturn(true);
        when(attentionRepository.existsBySessionIdAndHealthRecordId(100L, 200L)).thenReturn(false);

        handler.onHealthRecordAnalyzed(event(PredictionLabel.AFIB));

        verify(attentionRepository).save(any(ConsultationHealthRecordAttention.class));
    }

    @Test
    void highRiskNeverExpandsScopeFromTimestampCoincidence() {
        HealthRecord record = record();
        ConsultationSession session = session();
        when(healthRecordRepository.findByIdAndUserId(200L, 1L)).thenReturn(Optional.of(record));
        when(sessionRepository.findAllByMemberIdAndStatus(1L, ConsultationStatus.ACTIVE)).thenReturn(List.of(session));
        when(authorizationRepository.existsBySessionIdAndHealthRecordId(100L, 200L)).thenReturn(false);

        handler.onHealthRecordAnalyzed(event(PredictionLabel.AFIB));

        verify(attentionRepository, never()).save(any());
    }

    @Test
    void nonAfibRecordDoesNotInspectEpisodeScope() {
        handler.onHealthRecordAnalyzed(event(PredictionLabel.NORMAL));
        verifyNoInteractions(healthRecordRepository, sessionRepository, authorizationRepository, attentionRepository);
    }

    private HealthRecordAnalyzedEvent event(PredictionLabel label) {
        return HealthRecordAnalyzedEvent.builder()
                .recordId(200L).userId(1L).predictionLabel(label).confidence(0.95)
                .analyzedAt(Instant.now()).build();
    }

    private ConsultationSession session() {
        return ConsultationSession.builder()
                .id(100L).memberId(1L).doctorId(2L).status(ConsultationStatus.ACTIVE)
                .startedAt(Instant.parse("2026-08-10T08:00:00Z"))
                .activatedAt(Instant.now()).endsAt(Instant.parse("2026-08-17T08:00:00Z"))
                .build();
    }

    private HealthRecord record() {
        HealthRecord record = HealthRecord.builder()
                .id(200L).userId(1L).fileName("record.csv")
                .s3FileKey("records/record.csv").predictionLabel(PredictionLabel.AFIB)
                .build();
        record.setCreatedAt(Instant.parse("2026-08-10T08:30:00Z"));
        return record;
    }
}
