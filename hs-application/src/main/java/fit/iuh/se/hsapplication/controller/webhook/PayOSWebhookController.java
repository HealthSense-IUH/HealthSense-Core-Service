package fit.iuh.se.hsapplication.controller.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.se.hschat.service.payment.ConsultationPaymentService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
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
    ObjectMapper objectMapper;

    @GetMapping({"", "/"})
    public ApiResponse<String> health() {
        return new ApiResponse<>("payOS webhook endpoint is ready");
    }

    @PostMapping({"", "/"})
    public ApiResponse<Void> handlePayOSWebhook(@RequestBody(required = false) String body) throws Exception {
        if (body == null || body.trim().isEmpty())
            return new ApiResponse<>();

        JsonNode root = objectMapper.readTree(body);
        if (!root.hasNonNull("data") || !root.hasNonNull("signature")) {
            log.info("Received payOS webhook validation ping");
            return new ApiResponse<>();
        }

        Webhook webhook = objectMapper.treeToValue(root, Webhook.class);
        consultationPaymentService.handlePayOSWebhook(webhook);
        return new ApiResponse<>();
    }
}
