package fit.iuh.se.hsoperations.event;

import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Cửa phát sự kiện nghiệp vụ dùng chung cho toàn hệ thống.
 *
 * Giữ nguyên chữ ký record()/resolveNeedsAction() của OperationalEventService
 * để các call site không phải đổi; bên trong chỉ publish application event —
 * việc ghi audit/needs-action/notification do {@link OperationalEventRelay}
 * đảm nhận tại một chỗ duy nhất.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OperationalEventPublisher {

    ApplicationEventPublisher eventPublisher;

    public void record(OperationalEventCommand command) {
        eventPublisher.publishEvent(new OperationalEventRequested(command));
    }

    public void resolveNeedsAction(String idempotencyKey, String resolution) {
        eventPublisher.publishEvent(new NeedsActionResolutionRequested(idempotencyKey, resolution));
    }
}
