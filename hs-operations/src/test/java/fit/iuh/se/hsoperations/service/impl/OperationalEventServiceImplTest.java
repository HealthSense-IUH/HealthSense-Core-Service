package fit.iuh.se.hsoperations.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.se.hsoperations.dto.command.NeedsActionIntent;
import fit.iuh.se.hsoperations.dto.command.NotificationIntent;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.entity.BusinessAuditEvent;
import fit.iuh.se.hsoperations.entity.NeedsActionItem;
import fit.iuh.se.hsoperations.entity.NotificationProjectionTask;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.event.NotificationProjectionRequested;
import fit.iuh.se.hsoperations.repository.BusinessAuditEventRepository;
import fit.iuh.se.hsoperations.repository.NeedsActionItemRepository;
import fit.iuh.se.hsoperations.repository.NotificationProjectionTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationalEventServiceImplTest {
    @Mock
    BusinessAuditEventRepository auditRepository;
    @Mock
    NeedsActionItemRepository needsActionRepository;
    @Mock
    NotificationProjectionTaskRepository projectionRepository;
    @Mock
    ApplicationEventPublisher publisher;
    OperationalEventServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OperationalEventServiceImpl(auditRepository, needsActionRepository, projectionRepository,
                new ObjectMapper(), publisher);
    }

    @Test
    void persistsAuditWorkAndNotificationIntentWhileRemovingSensitiveMetadata() {
        BusinessAuditEvent persisted = BusinessAuditEvent.builder().id(90L).domainType(BusinessDomainType.PAYMENT)
                .domainId(10L).eventType(BusinessEventType.PAYMENT_REQUIRES_REVIEW)
                .actorType(BusinessActorType.SYSTEM).build();
        when(auditRepository.save(any())).thenReturn(persisted);
        when(needsActionRepository.findByIdempotencyKey("work:10")).thenReturn(Optional.empty());
        when(projectionRepository.save(any())).thenAnswer(invocation -> {
            NotificationProjectionTask task = invocation.getArgument(0);
            task.setId(91L);
            return task;
        });

        service.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.PAYMENT).domainId(10L)
                .eventType(BusinessEventType.PAYMENT_REQUIRES_REVIEW).actorType(BusinessActorType.SYSTEM)
                .idempotencyKey("audit:10").metadata(Map.of("providerCode", "PAID", "rawPayload", "secret data"))
                .needsAction(new NeedsActionIntent(NeedsActionType.PAYMENT_REQUIRES_REVIEW,
                        NeedsActionPriority.HIGH, "Review", "Review payment", BusinessDomainType.PAYMENT,
                        10L, "CARE_COORDINATOR", "work:10"))
                .notifications(List.of(new NotificationIntent(1L, NotificationType.PAYMENT_REQUIRES_REVIEW,
                        "Payment review", "Payment requires review.", BusinessDomainType.PAYMENT, 10L, "notice:10")))
                .build());

        ArgumentCaptor<BusinessAuditEvent> audit = ArgumentCaptor.forClass(BusinessAuditEvent.class);
        verify(auditRepository).save(audit.capture());
        assertTrue(audit.getValue().getMetadataJson().contains("providerCode"));
        assertFalse(audit.getValue().getMetadataJson().contains("secret data"));
        verify(needsActionRepository).save(any(NeedsActionItem.class));
        verify(publisher).publishEvent(new NotificationProjectionRequested(91L));
    }

    @Test
    void duplicateBusinessTransitionReturnsExistingEventWithoutDuplicateProjection() {
        BusinessAuditEvent existing = BusinessAuditEvent.builder().id(1L).domainType(BusinessDomainType.REQUEST)
                .domainId(2L).eventType(BusinessEventType.REQUEST_CREATED).actorType(BusinessActorType.USER).build();
        when(auditRepository.findByIdempotencyKey("request:2:created")).thenReturn(Optional.of(existing));

        assertSame(existing, service.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.REQUEST).domainId(2L).eventType(BusinessEventType.REQUEST_CREATED)
                .idempotencyKey("request:2:created").build()));

        verify(auditRepository, never()).save(any());
        verifyNoInteractions(needsActionRepository, projectionRepository, publisher);
    }

    @Test
    void durableWorkFailureFailsTheWholeAuditOperationBeforeProjection() {
        BusinessAuditEvent persisted = BusinessAuditEvent.builder().id(90L)
                .domainType(BusinessDomainType.PAYMENT).domainId(10L)
                .eventType(BusinessEventType.PAYMENT_REQUIRES_REVIEW)
                .actorType(BusinessActorType.SYSTEM).build();
        when(auditRepository.save(any())).thenReturn(persisted);
        when(needsActionRepository.findByIdempotencyKey("work:10")).thenReturn(Optional.empty());
        when(needsActionRepository.save(any())).thenThrow(new IllegalStateException("work persistence failed"));

        assertThrows(IllegalStateException.class, () -> service.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.PAYMENT).domainId(10L)
                .eventType(BusinessEventType.PAYMENT_REQUIRES_REVIEW)
                .idempotencyKey("audit:10")
                .needsAction(new NeedsActionIntent(NeedsActionType.PAYMENT_REQUIRES_REVIEW,
                        NeedsActionPriority.HIGH, "Review", "Review payment", BusinessDomainType.PAYMENT,
                        10L, "CARE_COORDINATOR", "work:10"))
                .notifications(List.of(new NotificationIntent(1L, NotificationType.PAYMENT_REQUIRES_REVIEW,
                        "Review", "Review payment", BusinessDomainType.PAYMENT, 10L, "notice:10")))
                .build()));

        verify(auditRepository).save(any());
        verifyNoInteractions(projectionRepository, publisher);
    }

    @Test
    void auditFailureCannotLeaveAWorkItemOrProjectionBehind() {
        when(auditRepository.save(any())).thenThrow(new IllegalStateException("audit persistence failed"));

        assertThrows(IllegalStateException.class, () -> service.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.REFUND).domainId(20L)
                .eventType(BusinessEventType.REFUND_REVIEW_REQUIRED).idempotencyKey("audit:20")
                .needsAction(new NeedsActionIntent(NeedsActionType.REFUND_REVIEW_REQUIRED,
                        NeedsActionPriority.HIGH, "Review", "Review refund", BusinessDomainType.REFUND,
                        20L, "CARE_COORDINATOR", "work:20"))
                .build()));

        verifyNoInteractions(needsActionRepository, projectionRepository, publisher);
    }
}
