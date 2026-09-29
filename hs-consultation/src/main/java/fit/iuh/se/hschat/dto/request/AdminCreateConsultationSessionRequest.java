package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminCreateConsultationSessionRequest {

    @NotNull(message = "{validation.patient-id-required}")
    Long memberId;

    @NotNull(message = "{validation.doctor-id-required}")
    Long doctorId;

    Long packageId;

    Long healthRecordId;

    Instant startedAt;

    @NotNull(message = "{validation.consultation-expiry-required}")
    @Future(message = "{validation.consultation-expiry-future}")
    Instant endsAt;

    @Future(message = "{validation.support-end-future}")
    Instant supportEndsAt;

    String initialSystemMessage;

    @NotBlank(message = "{validation.override-reason-required}")
    String overrideReason;

    @NotBlank(message = "{validation.override-service-scope-required}")
    @jakarta.validation.constraints.Size(max = 2000)
    String serviceScope;
}
