package fit.iuh.se.hsapplication.dto.response;

import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hsuser.dto.response.UserResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MemberDetailResponse {

    UserResponse user;
    HealthRecordResponse latestHealthRecord;
    Long totalHealthRecords;
}
