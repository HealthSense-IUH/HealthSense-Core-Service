package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConfirmConsultationRequest(@NotBlank String offerId) {
}
