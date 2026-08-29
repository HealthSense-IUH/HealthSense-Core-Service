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
public class CreateFinalSummaryAddendumRequest {

    @NotBlank
    @Size(max = 1000)
    String reason;

    @NotBlank
    @Size(max = 10000)
    String content;
}
