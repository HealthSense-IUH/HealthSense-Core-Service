package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.DoctorIneligibilityReason;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorCandidateResponse {

    Long doctorId;
    String displayName;
    String email;
    String phone;
    DoctorSpecialty specialty;
    Boolean acceptsOneOnOneCare;
    Long effectiveLoad;
    Integer maxActiveConsultations;
    String declaredSupportSchedule;
    String timezone;
    Boolean preferredByMember;
    Boolean eligible;
    List<DoctorIneligibilityReason> ineligibleReasons;
}
