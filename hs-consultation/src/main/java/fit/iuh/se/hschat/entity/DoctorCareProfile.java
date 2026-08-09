package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "doctor_care_profiles",
        indexes = {
                @Index(name = "idx_doctor_care_profile_specialty", columnList = "specialty"),
                @Index(name = "idx_doctor_care_profile_accepts", columnList = "accepts_one_on_one_care")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_doctor_care_profile_doctor", columnNames = "doctor_id")
        }
)
public class DoctorCareProfile extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "doctor_id", nullable = false, unique = true)
    Long doctorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialty", length = 40)
    DoctorSpecialty specialty;

    @Column(name = "accepts_one_on_one_care", nullable = false)
    Boolean acceptsOneOnOneCare;

    @Column(name = "max_active_consultations", nullable = false)
    Integer maxActiveConsultations;

    @Column(name = "availability_json", columnDefinition = "TEXT")
    String availabilityJson;

    @Column(name = "timezone", nullable = false, length = 80)
    String timezone;
}
