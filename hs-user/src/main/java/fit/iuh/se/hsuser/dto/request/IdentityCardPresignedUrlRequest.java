package fit.iuh.se.hsuser.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IdentityCardPresignedUrlRequest {

    @NotBlank(message = "Tên file ảnh không được để trống")
    String fileName;

    String contentType;

    String cardSide; // "FRONT" | "BACK"
}
