package fit.iuh.se.hschat.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifiedPayOSPayment {

    Long orderCode;
    Long amount;
    String currency;
    String paymentLinkId;
}
