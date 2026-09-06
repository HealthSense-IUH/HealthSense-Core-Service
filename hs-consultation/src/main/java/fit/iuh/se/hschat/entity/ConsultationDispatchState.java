package fit.iuh.se.hschat.entity;

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
@Table(name = "consultation_dispatch_state")
public class ConsultationDispatchState {

    public static final Long SINGLETON_ID = 1L;

    @Id
    @Column(nullable = false, updatable = false)
    Long id;

    @Column(name = "last_doctor_id")
    Long lastDoctorId;

    @Version
    @Column(nullable = false)
    long version;
}
