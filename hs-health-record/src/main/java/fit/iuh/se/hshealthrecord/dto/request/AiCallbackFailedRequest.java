package fit.iuh.se.hshealthrecord.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiCallbackFailedRequest {

    @NotNull(message = "ID bản ghi không được để trống")
    Long recordId;

    String errorReason;
}
