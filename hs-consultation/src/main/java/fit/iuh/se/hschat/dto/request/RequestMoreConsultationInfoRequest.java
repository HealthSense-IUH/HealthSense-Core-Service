package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestMoreConsultationInfoRequest {

    @NotBlank(message = "{validation.more-info-request-reason-required}")
    @Size(max = 500, message = "{validation.more-info-request-reason-max-500}")
    String reason;

    @Size(max = 120, message = "{validation.requested-items-category-max-120}")
    String requestedItemsCategory;
}
