package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateConsultationRequest {

    @NotNull(message = "Gói chăm sóc không được để trống")
    Long packageId;

    Long healthRecordId;

    @Deprecated
    String reason;

    @NotBlank(message = "Lý do chăm sóc không được để trống")
    @Size(max = 1000, message = "Lý do chăm sóc không được vượt quá 1000 ký tự")
    String reasonForCare;

    @NotBlank(message = "Mối quan tâm hiện tại không được để trống")
    @Size(max = 2000, message = "Mối quan tâm hiện tại không được vượt quá 2000 ký tự")
    String currentConcern;

    @Size(max = 1000, message = "Mục tiêu chăm sóc không được vượt quá 1000 ký tự")
    String careGoal;

    @Size(max = 1000, message = "Ghi chú của thành viên không được vượt quá 1000 ký tự")
    String memberNote;

    @Size(max = 4000, message = "Bối cảnh tự khai không được vượt quá 4000 ký tự")
    String relevantSelfReportedContext;

    List<Long> selectedHealthRecordIds;

    Long preferredDoctorId;
}
