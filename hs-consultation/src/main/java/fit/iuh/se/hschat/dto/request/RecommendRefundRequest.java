package fit.iuh.se.hschat.dto.request;

import fit.iuh.se.hschat.entity.enums.RefundRecommendation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecommendRefundRequest {
    @NotNull RefundRecommendation recommendation;
    @DecimalMin(value = "0.01") BigDecimal recommendedAmount;
    @NotBlank @Size(max = 1000) String reason;
    @Size(max = 4000) String operationalContext;
}
