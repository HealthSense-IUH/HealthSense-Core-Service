package fit.iuh.se.hsoperations.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.se.hsoperations.dto.command.*;
import fit.iuh.se.hsoperations.entity.*;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.event.NotificationProjectionRequested;
import fit.iuh.se.hsoperations.repository.*;
import fit.iuh.se.hsoperations.service.OperationalEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

@RequiredArgsConstructor
public class OperationalEventServiceImpl implements OperationalEventService {
    private static final Set<String> FORBIDDEN_METADATA_KEYS = Set.of(
            "content", "message", "raw", "payload", "token", "secret", "password", "file", "diagnosis");

    private final BusinessAuditEventRepository auditRepository;
    private final NeedsActionItemRepository needsActionRepository;
    private final NotificationProjectionTaskRepository projectionRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public BusinessAuditEvent record(OperationalEventCommand command) {
        validate(command);
        if (StringUtils.hasText(command.idempotencyKey())) {
            Optional<BusinessAuditEvent> existing = auditRepository.findByIdempotencyKey(command.idempotencyKey());
            if (existing.isPresent()) return existing.get();
        }

        BusinessAuditEvent audit = auditRepository.save(BusinessAuditEvent.builder()
                .domainType(command.domainType()).domainId(command.domainId()).eventType(command.eventType())
                .actorType(command.actorType() == null ? BusinessActorType.SYSTEM : command.actorType())
                .actorUserId(command.actorUserId()).actorRole(trim(command.actorRole(), 40))
                .requestId(command.requestId()).agreementId(command.agreementId()).paymentId(command.paymentId())
                .sessionId(command.sessionId()).renewalId(command.renewalId()).refundId(command.refundId())
                .healthRecordId(command.healthRecordId()).memberId(command.memberId()).doctorId(command.doctorId())
                .summaryId(command.summaryId()).previousState(trim(command.previousState(), 80))
                .newState(trim(command.newState(), 80)).reason(trim(command.reason(), 1000))
                .metadataJson(toSafeMetadata(command.metadata())).correctionOfEventId(command.correctionOfEventId())
                .idempotencyKey(trim(command.idempotencyKey(), 255)).occurredAt(command.occurredAt()).build());

        createNeedsAction(command, audit);
        createProjection(command, audit);
        return audit;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void resolveNeedsAction(String idempotencyKey, String resolution) {
        needsActionRepository.findByIdempotencyKey(idempotencyKey).ifPresent(item -> {
            if (item.getStatus() == NeedsActionStatus.RESOLVED) return;
            item.setStatus(NeedsActionStatus.RESOLVED);
            item.setResolvedAt(Instant.now());
            item.setResolution(trim(resolution, 1000));
            needsActionRepository.save(item);
        });
    }

    private void createNeedsAction(OperationalEventCommand command, BusinessAuditEvent audit) {
        NeedsActionIntent intent = command.needsAction();
        if (intent == null || needsActionRepository.findByIdempotencyKey(intent.idempotencyKey()).isPresent()) return;
        needsActionRepository.save(NeedsActionItem.builder()
                .type(intent.type()).status(NeedsActionStatus.OPEN).priority(intent.priority())
                .title(trim(intent.title(), 200)).description(trim(intent.description(), 1000))
                .referenceType(intent.referenceType()).referenceId(intent.referenceId())
                .requestId(command.requestId()).paymentId(command.paymentId()).sessionId(command.sessionId())
                .renewalId(command.renewalId()).refundId(command.refundId()).memberId(command.memberId())
                .doctorId(command.doctorId()).assignedRole(trim(intent.assignedRole(), 40))
                .idempotencyKey(trim(intent.idempotencyKey(), 255)).build());
    }

    private void createProjection(OperationalEventCommand command, BusinessAuditEvent audit) {
        List<NotificationIntent> notifications = command.notifications() == null ? List.of() : command.notifications();
        if (notifications.isEmpty()) return;
        try {
            NotificationProjectionTask task = projectionRepository.save(NotificationProjectionTask.builder()
                    .auditEventId(audit.getId()).payloadJson(objectMapper.writeValueAsString(notifications))
                    .status(NotificationProjectionStatus.PENDING).attempts(0).nextAttemptAt(Instant.now()).build());
            eventPublisher.publishEvent(new NotificationProjectionRequested(task.getId()));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Notification intent is not serializable", exception);
        }
    }

    private String toSafeMetadata(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) return null;
        Map<String, String> safe = new TreeMap<>();
        metadata.forEach((key, value) -> {
            String normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
            if (FORBIDDEN_METADATA_KEYS.stream().noneMatch(normalized::contains)) {
                safe.put(trim(key, 80), trim(value, 500));
            }
        });
        if (safe.isEmpty()) return null;
        try {
            return trim(objectMapper.writeValueAsString(safe), 4000);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Audit metadata is not serializable", exception);
        }
    }

    private void validate(OperationalEventCommand command) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(command.domainType(), "domainType");
        Objects.requireNonNull(command.domainId(), "domainId");
        Objects.requireNonNull(command.eventType(), "eventType");
    }

    private String trim(String value, int max) {
        if (!StringUtils.hasText(value)) return null;
        String stripped = value.strip();
        return stripped.length() <= max ? stripped : stripped.substring(0, max);
    }
}
