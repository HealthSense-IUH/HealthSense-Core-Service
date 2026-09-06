package fit.iuh.se.hschat.dto.request;

import fit.iuh.se.hschat.entity.enums.ContinuationDecision;
import jakarta.validation.constraints.NotNull;

public record SubmitContinuationDecisionRequest(
        @NotNull ContinuationDecision decision) {
}
