package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorCareProfileResponse {

    Long id;
    Long doctorId;
    DoctorSpecialty specialty;
    Boolean acceptsOneOnOneCare;
    Integer maxActiveConsultations;
    String availabilityJson;
    String timezone;
    Instant createdAt;
    Instant updatedAt;
}
