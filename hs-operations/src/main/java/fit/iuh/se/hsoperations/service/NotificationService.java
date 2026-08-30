package fit.iuh.se.hsoperations.service;

import fit.iuh.se.hsoperations.dto.response.*;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    PageResponse<NotificationResponse> findMine(Long recipientId, Pageable pageable);
    UnreadNotificationCountResponse unreadCount(Long recipientId);
    NotificationResponse markRead(Long recipientId, Long notificationId);
    void markAllRead(Long recipientId);
}
