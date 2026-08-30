package fit.iuh.se.hsoperations.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolveNeedsActionRequest(@NotBlank @Size(max = 1000) String resolution) {
}
