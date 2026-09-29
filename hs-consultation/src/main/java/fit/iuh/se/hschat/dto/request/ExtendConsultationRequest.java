package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExtendConsultationRequest {

    @NotNull(message = "{validation.new-consultation-expiry-required}")
    @Future(message = "{validation.new-consultation-expiry-future}")
    Instant endsAt;

    @Future(message = "{validation.new-support-end-future}")
    Instant supportEndsAt;

    @Size(max = 500, message = "{validation.renewal-reason-max-500}")
    String reason;
}
