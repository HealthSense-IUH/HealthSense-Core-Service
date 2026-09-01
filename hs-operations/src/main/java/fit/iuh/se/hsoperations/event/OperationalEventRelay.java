package fit.iuh.se.hsoperations.event;

import fit.iuh.se.hsoperations.service.OperationalEventService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Điểm hạ cánh DUY NHẤT của mọi sự kiện nghiệp vụ trong hệ thống.
 *
 * Chủ đích dùng @EventListener (đồng bộ, cùng thread) chứ KHÔNG dùng
 * @TransactionalEventListener/@Async: OperationalEventService.record yêu cầu
 * Propagation.MANDATORY — listener phải chạy bên trong transaction đang mở
 * của service nghiệp vụ, bảo đảm audit/notification được ghi nguyên tử cùng
 * thay đổi nghiệp vụ, y hệt hành vi gọi trực tiếp trước đây.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OperationalEventRelay {

    OperationalEventService operationalEventService;

    @EventListener
    public void on(OperationalEventRequested event) {
        operationalEventService.record(event.command());
    }

    @EventListener
    public void on(NeedsActionResolutionRequested event) {
        operationalEventService.resolveNeedsAction(event.idempotencyKey(), event.resolution());
    }
}
