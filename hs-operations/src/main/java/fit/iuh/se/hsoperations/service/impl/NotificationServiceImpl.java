package fit.iuh.se.hsoperations.service.impl;

import fit.iuh.se.hsoperations.dto.response.*;
import fit.iuh.se.hsoperations.entity.UserNotification;
import fit.iuh.se.hsoperations.repository.UserNotificationRepository;
import fit.iuh.se.hsoperations.service.NotificationService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final UserNotificationRepository repository;

    @Override @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> findMine(Long recipientId, Pageable pageable) {
        return new PageResponse<>(repository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable)
                .map(NotificationResponse::from));
    }

    @Override @Transactional(readOnly = true)
    public UnreadNotificationCountResponse unreadCount(Long recipientId) {
        return new UnreadNotificationCountResponse(repository.countByRecipientIdAndReadAtIsNull(recipientId));
    }

    @Override @Transactional
    public NotificationResponse markRead(Long recipientId, Long notificationId) {
        UserNotification notification = repository.findByIdAndRecipientId(notificationId, recipientId)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Notification not found"));
        if (notification.getReadAt() == null) notification.setReadAt(Instant.now());
        return NotificationResponse.from(repository.save(notification));
    }

    @Override @Transactional
    public void markAllRead(Long recipientId) {
        repository.markAllRead(recipientId, Instant.now());
    }
}
