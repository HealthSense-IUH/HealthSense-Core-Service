package fit.iuh.se.hsapplication.realtime;

import fit.iuh.se.hsoperations.dto.command.NotificationIntent;
import fit.iuh.se.hsoperations.event.OperationalEventRequested;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

/**
 * Nửa realtime của hệ thống sự kiện: nghe cùng OperationalEventRequested như
 * OperationalEventRelay, nhưng thay vì ghi database thì ĐẨY "ping" qua
 * WebSocket để chuông thông báo trên trình duyệt nhảy tức thì (thay polling).
 *
 * Chủ đích:
 * - AFTER_COMMIT (khác Relay chạy trong transaction): chỉ đẩy khi dữ liệu đã
 *   commit chắc chắn — không bao giờ báo "ma" về một giao dịch bị rollback.
 * - Chỉ đẩy PING (loại thông báo, không kèm nội dung): client nhận ping thì
 *   tự gọi REST lấy số chưa đọc/danh sách — nội dung đi qua đường có sẵn
 *   được phân quyền đầy đủ, kênh WS không mang dữ liệu nhạy cảm.
 * - Đích /queue/notifications/{userId} và /topic/notifications/roles/{ROLE}
 *   được gác quyền subscribe trong WebSocketConfig.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationRealtimePushListener {

    SimpMessageSendingOperations messagingTemplate;

    @NonFinal
    @Value("${app.websocket.topic-prefix}")
    String topicPrefix;

    @NonFinal
    @Value("${app.websocket.queue-prefix}")
    String queuePrefix;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OperationalEventRequested event) {
        List<NotificationIntent> notifications = event.command().notifications();
        if (notifications == null || notifications.isEmpty())
            return;

        for (NotificationIntent intent : notifications) {
            try {
                Map<String, String> ping = Map.of("type", intent.type().name());
                if (intent.recipientId() != null) {
                    messagingTemplate.convertAndSend(
                            queuePrefix + "/notifications/" + intent.recipientId(), ping);
                } else if (intent.recipientRole() != null) {
                    messagingTemplate.convertAndSend(
                            topicPrefix + "/notifications/roles/" + intent.recipientRole().name(), ping);
                }
            } catch (RuntimeException exception) {
                // Đẩy realtime là best-effort: hỏng thì client vẫn thấy thông báo
                // qua REST — tuyệt đối không để lỗi WS ảnh hưởng luồng nghiệp vụ.
                log.warn("Realtime notification push failed: {}", exception.getMessage());
            }
        }
    }
}
