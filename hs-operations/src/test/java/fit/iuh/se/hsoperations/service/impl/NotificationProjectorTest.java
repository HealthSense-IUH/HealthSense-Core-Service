package fit.iuh.se.hsoperations.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.se.hsoperations.dto.command.NotificationIntent;
import fit.iuh.se.hsoperations.entity.NotificationProjectionTask;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.repository.*;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.*;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationProjectorTest {
    @Mock NotificationProjectionTaskRepository taskRepository;
    @Mock UserNotificationRepository notificationRepository;
    @Mock UserAccountRepository userAccountRepository;

    @Test
    void duplicateProjectionDoesNotDuplicateVisibleNotification() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(List.of(new NotificationIntent(1L,
                NotificationType.CARE_ACTIVATED, "Care active", "Care is active.",
                BusinessDomainType.SESSION, 10L, "session:10:member")));
        NotificationProjectionTask task = task(payload);
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task));
        when(notificationRepository.existsByIdempotencyKey("session:10:member")).thenReturn(true);

        new NotificationProjector(taskRepository, notificationRepository, userAccountRepository, mapper).project(2L);

        assertEquals(NotificationProjectionStatus.SUCCEEDED, task.getStatus());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void projectionFailureIsCapturedForRetryAndDoesNotEscape() {
        NotificationProjectionTask task = task("not-json");
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task));

        assertDoesNotThrow(() -> new NotificationProjector(taskRepository, notificationRepository, userAccountRepository,
                new ObjectMapper()).project(2L));

        assertEquals(NotificationProjectionStatus.FAILED, task.getStatus());
        assertNotNull(task.getNextAttemptAt());
        verify(taskRepository).save(task);
    }

    @Test
    void roleAudienceFansOutOnlyToActiveRoleMembersWithPerUserIdempotency() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(List.of(NotificationIntent.forRole(
                UserRole.CARE_COORDINATOR, NotificationType.REQUEST_RECEIVED, "New request",
                "A request needs coordination.", BusinessDomainType.REQUEST, 10L, "request:10:coordinators")));
        NotificationProjectionTask task = task(payload);
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task));
        when(userAccountRepository.findAllByRoleAndStatus(UserRole.CARE_COORDINATOR, AccountStatus.ACTIVE))
                .thenReturn(List.of(UserAccount.builder().id(7L).build(), UserAccount.builder().id(8L).build()));
        when(notificationRepository.existsByIdempotencyKey(anyString())).thenReturn(false);

        new NotificationProjector(taskRepository, notificationRepository, userAccountRepository, mapper).project(2L);

        assertEquals(NotificationProjectionStatus.SUCCEEDED, task.getStatus());
        verify(notificationRepository, times(2)).save(argThat(notification ->
                notification.getRecipientId().equals(7L) || notification.getRecipientId().equals(8L)));
    }

    private NotificationProjectionTask task(String payload) {
        return NotificationProjectionTask.builder().id(2L).auditEventId(1L).payloadJson(payload)
                .status(NotificationProjectionStatus.PENDING).attempts(0).nextAttemptAt(Instant.now()).build();
    }
}
