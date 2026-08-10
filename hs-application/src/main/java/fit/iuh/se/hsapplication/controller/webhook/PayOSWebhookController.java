package fit.iuh.se.hsapplication.controller.webhook;

import fit.iuh.se.hschat.service.payment.ConsultationPaymentService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.webhooks.Webhook;

@RestController
@RequestMapping("/api/webhooks/payos")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOSWebhookController {

    ConsultationPaymentService consultationPaymentService;

    @GetMapping
    public ApiResponse<String> health() {
        return new ApiResponse<>("payOS webhook endpoint is ready");
    }

    @PostMapping
    public ApiResponse<Void> handlePayOSWebhook(@RequestBody Webhook webhook) {
        consultationPaymentService.handlePayOSWebhook(webhook);
        return new ApiResponse<>();
    }
}
