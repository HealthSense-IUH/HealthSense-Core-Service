package fit.iuh.se.hschat.service.refund;

import fit.iuh.se.hschat.dto.request.DecideRefundRequest;
import fit.iuh.se.hschat.dto.request.RecommendRefundRequest;
import fit.iuh.se.hschat.dto.request.ReconcileRefundRequest;
import fit.iuh.se.hschat.dto.response.ConsultationRefundResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;

public interface ConsultationRefundService {
    ConsultationRefundResponse recommend(Long actorId, UserRole role, Long paymentId, RecommendRefundRequest request);
    ConsultationRefundResponse decide(Long actorId, UserRole role, Long refundId, DecideRefundRequest request);
    ConsultationRefundResponse execute(Long actorId, UserRole role, Long refundId);
    ConsultationRefundResponse reconcile(Long actorId, UserRole role, Long refundId, ReconcileRefundRequest request);
    ConsultationRefundResponse get(UserRole role, Long refundId);
}
