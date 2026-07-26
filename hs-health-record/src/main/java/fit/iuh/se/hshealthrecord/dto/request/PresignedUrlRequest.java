package fit.iuh.se.hshealthrecord.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PresignedUrlRequest {

    @NotBlank(message = "Tên file không được để trống")
    String fileName;

    @NotNull(message = "Dung lượng file không được để trống")
    @Positive(message = "Dung lượng file phải lớn hơn 0")
    Long fileSize;
}
