package fit.iuh.se.hsapplication.realtime;

import fit.iuh.se.hsoperations.dto.command.NotificationIntent;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.entity.enums.BusinessDomainType;
import fit.iuh.se.hsoperations.entity.enums.NotificationType;
import fit.iuh.se.hsoperations.event.OperationalEventRequested;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import org.mockito.ArgumentMatchers;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRealtimePushListenerTest {

    @Mock
    SimpMessageSendingOperations messagingTemplate;

    NotificationRealtimePushListener listener;

    @BeforeEach
    void setUp() {
        listener = new NotificationRealtimePushListener(messagingTemplate);
        ReflectionTestUtils.setField(listener, "topicPrefix", "/topic");
        ReflectionTestUtils.setField(listener, "queuePrefix", "/queue");
    }

    private OperationalEventRequested eventWith(NotificationIntent... intents) {
        return new OperationalEventRequested(OperationalEventCommand.builder()
                .notifications(intents.length == 0 ? null : List.of(intents))
                .build());
    }

    @Test
    void pushesToUserQueueForDirectRecipient() {
        listener.on(eventWith(new NotificationIntent(
                42L, NotificationType.PAYMENT_CONFIRMED, "title", "msg",
                BusinessDomainType.PAYMENT, 1L, "key-1")));

        verify(messagingTemplate).convertAndSend(
                (String) eq("/queue/notifications/42"),
                ArgumentMatchers.<Map<String, String>>eq(Map.of("type", "PAYMENT_CONFIRMED")));
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    void pushesToRoleTopicForRoleRecipient() {
        listener.on(eventWith(NotificationIntent.forRole(
                UserRole.CARE_COORDINATOR, NotificationType.OPERATIONAL_REVIEW_REQUIRED,
                "title", "msg", BusinessDomainType.PAYMENT, 1L, "key-2")));

        verify(messagingTemplate).convertAndSend(
                (String) eq("/topic/notifications/roles/CARE_COORDINATOR"),
                ArgumentMatchers.<Map<String, String>>eq(Map.of("type", "OPERATIONAL_REVIEW_REQUIRED")));
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    void doesNothingWhenEventHasNoNotifications() {
        listener.on(eventWith());

        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void pushesEachIntentIndependently() {
        listener.on(eventWith(
                new NotificationIntent(1L, NotificationType.CARE_ACTIVATED, "t", "m",
                        BusinessDomainType.SESSION, 9L, "key-3"),
                new NotificationIntent(2L, NotificationType.CARE_ACTIVATED, "t", "m",
                        BusinessDomainType.SESSION, 9L, "key-4")));

        verify(messagingTemplate).convertAndSend((String) eq("/queue/notifications/1"), ArgumentMatchers.<Map<String, String>>any());
        verify(messagingTemplate).convertAndSend((String) eq("/queue/notifications/2"), ArgumentMatchers.<Map<String, String>>any());
    }

    @Test
    void websocketFailureIsSwallowedAndRemainingIntentsStillPushed() {
        doThrow(new IllegalStateException("broker down"))
                .when(messagingTemplate)
                .convertAndSend((String) eq("/queue/notifications/1"), ArgumentMatchers.<Map<String, String>>any());

        listener.on(eventWith(
                new NotificationIntent(1L, NotificationType.NEW_MESSAGE, "t", "m",
                        BusinessDomainType.SESSION, 9L, "key-5"),
                new NotificationIntent(2L, NotificationType.NEW_MESSAGE, "t", "m",
                        BusinessDomainType.SESSION, 9L, "key-6")));

        // Không ném lỗi ra ngoài (không phá luồng nghiệp vụ) và intent sau vẫn được đẩy
        verify(messagingTemplate).convertAndSend((String) eq("/queue/notifications/2"), ArgumentMatchers.<Map<String, String>>any());
    }
}
