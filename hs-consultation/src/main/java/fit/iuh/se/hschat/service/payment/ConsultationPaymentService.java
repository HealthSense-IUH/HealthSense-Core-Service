package fit.iuh.se.hschat.service.payment;

import fit.iuh.se.hschat.dto.response.ConsultationPaymentResponse;
import vn.payos.model.webhooks.Webhook;

public interface ConsultationPaymentService {

    ConsultationPaymentResponse createPayment(Long memberId, Long requestId);

    ConsultationPaymentResponse getPayment(Long memberId, Long requestId);

    void handlePayOSWebhook(Webhook webhook);

    void expireOverduePayments();
}
