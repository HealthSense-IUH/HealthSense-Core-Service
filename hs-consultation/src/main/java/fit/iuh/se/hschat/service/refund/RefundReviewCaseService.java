package fit.iuh.se.hschat.service.refund;

import fit.iuh.se.hschat.entity.ConsultationPayment;

public interface RefundReviewCaseService {
    void ensureReviewRequired(ConsultationPayment payment);
}
