package fit.iuh.se.hschat.dto.request;

import fit.iuh.se.hschat.entity.enums.CareServiceCode;
import fit.iuh.se.hschat.entity.enums.CareServiceSupportPolicy;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCareServicePackageRequest {

    @NotBlank(message = "Mã gói không được để trống")
    @Size(max = 80, message = "Mã gói không được vượt quá 80 ký tự")
    String code;

    @NotBlank(message = "Tên gói không được để trống")
    @Size(max = 160, message = "Tên gói không được vượt quá 160 ký tự")
    String name;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    String description;

    @Size(max = 500, message = "Mô tả ngắn không được vượt quá 500 ký tự")
    String shortDescription;

    @Size(max = 4000, message = "Mô tả chi tiết không được vượt quá 4000 ký tự")
    String detailedDescription;

    @NotNull(message = "Giá gói không được để trống")
    @DecimalMin(value = "0.01", message = "Giá gói phải lớn hơn 0")
    BigDecimal priceAmount;

    @Size(min = 3, max = 3, message = "Mã tiền tệ phải có 3 ký tự")
    String currency;

    @NotNull(message = "Thời lượng gói không được để trống")
    @Min(value = 1, message = "Thời lượng gói phải lớn hơn 0")
    Integer durationDays;

    List<CareServiceCode> includedServices;

    List<CareServiceCode> excludedServices;

    DoctorSpecialty requiredSpecialty;

    CareServiceSupportPolicy supportPolicy;

    @NotNull(message = "Trạng thái gia hạn không được để trống")
    Boolean renewable;

    @Size(max = 255, message = "Tham chiếu điều khoản không được vượt quá 255 ký tự")
    String termsPolicyReference;
}
