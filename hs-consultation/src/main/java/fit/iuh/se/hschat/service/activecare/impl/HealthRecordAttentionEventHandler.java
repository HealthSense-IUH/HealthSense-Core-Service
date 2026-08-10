package fit.iuh.se.hschat.service.activecare.impl;

import fit.iuh.se.hschat.entity.ConsultationHealthRecordAttention;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationAttentionReason;
import fit.iuh.se.hschat.entity.enums.ConsultationAttentionStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.repository.ConsultationHealthRecordAttentionRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.event.HealthRecordAnalyzedEvent;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HealthRecordAttentionEventHandler {

    ConsultationSessionRepository sessionRepository;
    ConsultationHealthRecordAttentionRepository attentionRepository;
    HealthRecordRepository healthRecordRepository;

    @EventListener
    @Transactional
    public void onHealthRecordAnalyzed(HealthRecordAnalyzedEvent event) {
        if (event.getPredictionLabel() != PredictionLabel.AFIB)
            return;

        HealthRecord record = healthRecordRepository.findByIdAndUserId(event.getRecordId(), event.getUserId())
                .orElse(null);
        if (record == null)
            return;

        sessionRepository.findAllByMemberIdAndStatus(event.getUserId(), ConsultationStatus.ACTIVE)
                .stream()
                .filter(session -> isRecordInScope(session, record))
                .forEach(session -> createAttentionIfAbsent(session, record));
    }

    private void createAttentionIfAbsent(ConsultationSession session, HealthRecord record) {
        if (attentionRepository.existsBySessionIdAndHealthRecordId(session.getId(), record.getId()))
            return;

        try {
            attentionRepository.save(ConsultationHealthRecordAttention.builder()
                    .sessionId(session.getId())
                    .healthRecordId(record.getId())
                    .status(ConsultationAttentionStatus.REQUIRES_ATTENTION)
                    .reason(ConsultationAttentionReason.AFIB)
                    .build());
        } catch (DataIntegrityViolationException exception) {
            log.info("Consultation attention already exists for session {} and record {}", session.getId(), record.getId());
        }
    }

    private boolean isRecordInScope(ConsultationSession session, HealthRecord record) {
        if (!record.getUserId().equals(session.getMemberId()))
            return false;
        if (Objects.equals(record.getId(), session.getHealthRecordId()))
            return true;
        Instant createdAt = record.getCreatedAt();
        return session.getStartedAt() != null
                && session.getEndsAt() != null
                && createdAt != null
                && !createdAt.isBefore(session.getStartedAt())
                && !createdAt.isAfter(session.getEndsAt());
    }
}
