package fit.iuh.se.hschat.service.payment;

import fit.iuh.se.hschat.dto.response.ConsultationPaymentResponse;
import vn.payos.model.webhooks.Webhook;

import java.util.List;

public interface ConsultationPaymentService {

    ConsultationPaymentResponse createPayment(Long memberId, Long requestId);

    ConsultationPaymentResponse getPayment(Long memberId, Long requestId);

    List<ConsultationPaymentResponse> getPaymentAttempts(Long memberId, Long requestId);

    ConsultationPaymentResponse createRenewalPayment(Long memberId, Long renewalId);

    List<ConsultationPaymentResponse> getRenewalPaymentAttempts(Long memberId, Long renewalId);

    void handlePayOSWebhook(Webhook webhook);

    void expireOverduePayments();
}
