package fit.iuh.se.hschat.service.activecare.impl;

import fit.iuh.se.hschat.entity.ConsultationHealthRecordAttention;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationAttentionReason;
import fit.iuh.se.hschat.entity.enums.ConsultationAttentionStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.repository.ConsultationHealthRecordAttentionRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.EpisodeHealthRecordAuthorizationRepository;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.event.HealthRecordAnalyzedEvent;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hsoperations.dto.command.*;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HealthRecordAttentionEventHandler {

    ConsultationSessionRepository sessionRepository;
    ConsultationHealthRecordAttentionRepository attentionRepository;
    HealthRecordRepository healthRecordRepository;
    EpisodeHealthRecordAuthorizationRepository authorizationRepository;
    OperationalEventPublisher OperationalEventPublisher;

    @EventListener
    @Transactional
    public void onHealthRecordAnalyzed(HealthRecordAnalyzedEvent event) {
        if (event.getPredictionLabel() != PredictionLabel.AFIB && event.getPredictionLabel() != PredictionLabel.AFIB_SUSPECTED)
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
            ConsultationHealthRecordAttention attention = attentionRepository.save(ConsultationHealthRecordAttention.builder()
                    .sessionId(session.getId())
                    .healthRecordId(record.getId())
                    .status(ConsultationAttentionStatus.REQUIRES_ATTENTION)
                    .reason(ConsultationAttentionReason.AFIB)
                    .build());
            String key = "health-attention:" + session.getId() + ":" + record.getId();
            OperationalEventPublisher.record(OperationalEventCommand.builder()
                    .domainType(BusinessDomainType.HEALTH_RECORD).domainId(record.getId())
                    .eventType(BusinessEventType.HEALTH_ATTENTION_REQUIRED).actorType(BusinessActorType.SYSTEM)
                    .sessionId(session.getId()).healthRecordId(record.getId()).memberId(session.getMemberId())
                    .doctorId(session.getDoctorId()).newState(attention.getStatus().name()).idempotencyKey(key)
                    .notifications(java.util.List.of(
                            new NotificationIntent(session.getMemberId(), NotificationType.HEALTH_ATTENTION_REQUIRED,
                                    "Health result needs attention",
                                    "A health result needs clinical attention. This is not an emergency response service; seek local emergency help for urgent symptoms.",
                                    BusinessDomainType.HEALTH_RECORD, record.getId(), key + ":member"),
                            new NotificationIntent(session.getDoctorId(), NotificationType.HEALTH_ATTENTION_REQUIRED,
                                    "Authorized record needs review",
                                    "An authorized HealthRecord needs clinical review. No immediate-response guarantee is implied.",
                                    BusinessDomainType.HEALTH_RECORD, record.getId(), key + ":doctor")))
                    .build());
        } catch (DataIntegrityViolationException exception) {
            log.info("Consultation attention already exists for session {} and record {}", session.getId(), record.getId());
        }
    }

    private boolean isRecordInScope(ConsultationSession session, HealthRecord record) {
        return record.getUserId().equals(session.getMemberId())
                && session.getActivatedAt() != null
                && authorizationRepository.existsBySessionIdAndHealthRecordId(session.getId(), record.getId());
    }
}
