package fit.iuh.se.hschat.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayOSPaymentLink {

    Long orderCode;
    String paymentLinkId;
    String checkoutUrl;
    String status;
}
