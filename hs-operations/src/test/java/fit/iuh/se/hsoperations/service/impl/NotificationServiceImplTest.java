package fit.iuh.se.hsoperations.service.impl;

import fit.iuh.se.hsoperations.entity.UserNotification;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.repository.UserNotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
    @Mock UserNotificationRepository repository;

    @Test
    void markingNotificationReadDoesNotChangeItsBusinessReference() {
        UserNotification notification = UserNotification.builder().id(3L).recipientId(1L)
                .type(NotificationType.CARE_ACTIVATED).title("Care").message("Active")
                .referenceType(BusinessDomainType.SESSION).referenceId(10L).idempotencyKey("n:3")
                .deliveryStatus(NotificationDeliveryStatus.AVAILABLE).build();
        when(repository.findByIdAndRecipientId(3L, 1L)).thenReturn(Optional.of(notification));
        when(repository.save(notification)).thenReturn(notification);

        var result = new NotificationServiceImpl(repository).markRead(1L, 3L);

        assertTrue(result.read());
        assertEquals(10L, result.referenceId());
        assertNotNull(notification.getReadAt());
    }

    @Test
    void markingAllNotificationsReadOnlyMutatesRecipientNotificationRows() {
        NotificationServiceImpl service = new NotificationServiceImpl(repository);

        service.markAllRead(1L);

        verify(repository).markAllRead(eq(1L), any(Instant.class));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void unreadCountIsRecipientScoped() {
        when(repository.countByRecipientIdAndReadAtIsNull(1L)).thenReturn(4L);
        assertEquals(4L, new NotificationServiceImpl(repository).unreadCount(1L).unreadCount());
        verify(repository).countByRecipientIdAndReadAtIsNull(1L);
    }
}
