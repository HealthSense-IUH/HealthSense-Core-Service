package fit.iuh.se.hsoperations.event;

import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;

/**
 * Application event yêu cầu ghi nhận một sự kiện nghiệp vụ
 * (audit + needs-action + notification projection).
 *
 * Các service nghiệp vụ KHÔNG gọi thẳng OperationalEventService nữa mà publish
 * event này qua {@link OperationalEventPublisher}; {@link OperationalEventRelay}
 * là nơi duy nhất nhận và ghi xuống. Listener chạy đồng bộ trong cùng
 * transaction của caller nên độ tin cậy giữ nguyên như gọi trực tiếp.
 */
public record OperationalEventRequested(OperationalEventCommand command) {
}
