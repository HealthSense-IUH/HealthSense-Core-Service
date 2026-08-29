package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationPaymentProvider;
import fit.iuh.se.hschat.entity.enums.ConsultationPaymentStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationPaymentPurpose;
import fit.iuh.se.hschat.entity.enums.PaymentProviderCancellationStatus;
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
    Long renewalId;
    ConsultationPaymentPurpose paymentPurpose;
    Long agreementId;
    Integer attemptNumber;
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
    PaymentProviderCancellationStatus providerCancellationStatus;
    Instant providerCancellationRequestedAt;
    Instant providerCancellationCompletedAt;
    Instant providerCancellationLastAttemptAt;
    String providerCancellationError;
    Instant createdAt;
    Instant updatedAt;
}
