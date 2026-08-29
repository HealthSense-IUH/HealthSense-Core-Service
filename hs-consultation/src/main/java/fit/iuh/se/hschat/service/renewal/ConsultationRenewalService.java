package fit.iuh.se.hschat.service.renewal;

import fit.iuh.se.hschat.dto.request.DecideConsultationRenewalRequest;
import fit.iuh.se.hschat.dto.request.RequestConsultationRenewalRequest;
import fit.iuh.se.hschat.dto.response.ConsultationRenewalResponse;
import fit.iuh.se.hschat.dto.response.SessionExtensionResponse;
import fit.iuh.se.hschat.entity.ConsultationPayment;
import fit.iuh.se.hschat.entity.ConsultationRenewal;
import fit.iuh.se.hsuser.entity.enums.UserRole;

import java.time.Instant;
import java.util.List;

public interface ConsultationRenewalService {
    ConsultationRenewalResponse request(Long memberId, Long sessionId, RequestConsultationRenewalRequest request);
    ConsultationRenewalResponse beginReview(Long actorId, UserRole role, Long renewalId);
    ConsultationRenewalResponse decide(Long actorId, UserRole role, Long renewalId, DecideConsultationRenewalRequest request);
    ConsultationRenewalResponse cancel(Long memberId, Long renewalId);
    List<ConsultationRenewalResponse> getMemberSessionRenewals(Long memberId, Long sessionId);
    List<SessionExtensionResponse> getMemberSessionExtensions(Long memberId, Long sessionId);
    ConsultationRenewal requireWaitingPaymentForUpdate(Long memberId, Long renewalId);
    ConsultationRenewal requireOwned(Long memberId, Long renewalId);
    void applyVerifiedPayment(ConsultationPayment payment, Instant now);
    void expireForPayment(ConsultationPayment payment, Instant now);
    void markRequiresReview(ConsultationPayment payment);
    void expireOverdueRenewals(Instant now);
}
