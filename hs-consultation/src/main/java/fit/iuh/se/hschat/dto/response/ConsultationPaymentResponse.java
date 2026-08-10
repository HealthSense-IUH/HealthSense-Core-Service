package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationPaymentProvider;
import fit.iuh.se.hschat.entity.enums.ConsultationPaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsultationPaymentResponse {

    Long id;
    Long requestId;
    Long memberId;
    ConsultationPaymentProvider provider;
    Long orderCode;
    String paymentLinkId;
    String checkoutUrl;
    BigDecimal amount;
    String currency;
    ConsultationPaymentStatus status;
    Instant expiresAt;
    Instant paidAt;
    Instant expiredAt;
    Instant cancelledAt;
    Instant createdAt;
    Instant updatedAt;
}
