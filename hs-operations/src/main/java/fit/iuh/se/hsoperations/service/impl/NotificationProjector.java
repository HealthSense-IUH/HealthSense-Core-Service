package fit.iuh.se.hsoperations.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.se.hsoperations.dto.command.NotificationIntent;
import fit.iuh.se.hsoperations.entity.NotificationProjectionTask;
import fit.iuh.se.hsoperations.entity.UserNotification;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.repository.*;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationProjector {
    private final NotificationProjectionTaskRepository taskRepository;
    private final UserNotificationRepository notificationRepository;
    private final UserAccountRepository userAccountRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void project(Long taskId) {
        NotificationProjectionTask task = taskRepository.findById(taskId).orElse(null);
        if (task == null || task.getStatus() == NotificationProjectionStatus.SUCCEEDED) return;
        task.setStatus(NotificationProjectionStatus.PROCESSING);
        task.setAttempts(task.getAttempts() + 1);
        try {
            List<NotificationIntent> intents = objectMapper.readValue(task.getPayloadJson(), new TypeReference<>() {});
            for (NotificationIntent intent : intents) {
                if (intent.recipientId() != null) {
                    saveNotification(intent, intent.recipientId(), intent.idempotencyKey());
                    continue;
                }
                if (intent.recipientRole() == null) continue;
                for (UserAccount recipient : userAccountRepository.findAllByRoleAndStatus(
                        intent.recipientRole(), AccountStatus.ACTIVE)) {
                    saveNotification(intent, recipient.getId(), intent.idempotencyKey() + ":" + recipient.getId());
                }
            }
            task.setStatus(NotificationProjectionStatus.SUCCEEDED);
            task.setCompletedAt(Instant.now());
            task.setLastError(null);
            task.setNextAttemptAt(null);
        } catch (Exception exception) {
            task.setStatus(NotificationProjectionStatus.FAILED);
            task.setLastError(truncate(exception.getMessage(), 1000));
            long delayMinutes = Math.min(60, 1L << Math.min(task.getAttempts(), 6));
            task.setNextAttemptAt(Instant.now().plus(delayMinutes, ChronoUnit.MINUTES));
        }
        taskRepository.save(task);
    }

    private void saveNotification(NotificationIntent intent, Long recipientId, String idempotencyKey) {
        if (notificationRepository.existsByIdempotencyKey(idempotencyKey)) return;
        notificationRepository.save(UserNotification.builder()
                .recipientId(recipientId).type(intent.type()).title(intent.title())
                .message(intent.message()).referenceType(intent.referenceType()).referenceId(intent.referenceId())
                .idempotencyKey(idempotencyKey).deliveryStatus(NotificationDeliveryStatus.AVAILABLE).build());
    }

    private String truncate(String value, int max) {
        if (value == null) return "Unknown notification projection failure";
        return value.length() <= max ? value : value.substring(0, max);
    }
}
