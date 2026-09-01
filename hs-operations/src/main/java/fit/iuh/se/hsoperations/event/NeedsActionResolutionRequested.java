package fit.iuh.se.hsoperations.event;

/**
 * Application event yêu cầu đóng một needs-action item theo idempotency key.
 * Đi cùng cơ chế với {@link OperationalEventRequested}.
 */
public record NeedsActionResolutionRequested(String idempotencyKey, String resolution) {
}
