package fit.iuh.se.hsapplication.controller.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.se.hschat.service.payment.ConsultationPaymentService;
import fit.iuh.se.hschat.service.payment.PayOSPaymentGateway;
import fit.iuh.se.hsbilling.service.CreditPayOSWebhookService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.webhooks.Webhook;

@RestController
@RequestMapping({
        "/api/webhooks/payos",
        "/public/payments/webhook/payos"
})
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOSWebhookController {

    ConsultationPaymentService consultationPaymentService;
    PayOSPaymentGateway paymentGateway;
    CreditPayOSWebhookService creditPayments;
    ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(value = {"", "/", "/**"}, method = {RequestMethod.GET, RequestMethod.HEAD})
    public ApiResponse<String> health() {
        return new ApiResponse<>("payOS webhook endpoint is ready");
    }

    @PostMapping({"", "/", "/**"})
    public ApiResponse<Void> handlePayOSWebhook(@RequestBody(required = false) String body) throws Exception {
        log.info("payOS webhook request received");

        if (body == null || body.trim().isEmpty()) {
            log.info("Received empty payOS webhook validation ping");
            return new ApiResponse<>();
        }

        JsonNode root = objectMapper.readTree(body);
        if (!root.hasNonNull("data") || !root.hasNonNull("signature")) {
            log.info("Received payOS webhook validation ping");
            return new ApiResponse<>();
        }

        Webhook webhook = objectMapper.treeToValue(root, Webhook.class);
        final fit.iuh.se.hschat.dto.VerifiedPayOSPayment verified;
        try { verified = paymentGateway.verifyWebhook(webhook); }
        catch (Exception exception) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_WEBHOOK, "Invalid PayOS webhook signature or payload");
        }
        boolean handled = creditPayments.handle(verified.getOrderCode(), verified.getAmount(),
                verified.getCurrency(), verified.getPaymentLinkId(), verified.getCode(), verified.getReference());
        if (!handled) consultationPaymentService.handleVerifiedPayOSWebhook(verified);
        return new ApiResponse<>();
    }
}
