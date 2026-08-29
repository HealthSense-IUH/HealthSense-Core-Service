package fit.iuh.se.hschat.service.payment;

public interface PaymentCancellationService {
    void prepareRequestCancellation(Long requestId);
    void cancelProviderLinksAfterCommit(Long requestId);
    void reconcileProviderCancellation(Long actorId, fit.iuh.se.hsuser.entity.enums.UserRole role, Long paymentId);
}
