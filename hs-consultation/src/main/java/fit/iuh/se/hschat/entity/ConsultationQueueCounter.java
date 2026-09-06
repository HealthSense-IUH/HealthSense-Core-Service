package fit.iuh.se.hschat.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "consultation_queue_counter")
public class ConsultationQueueCounter {

    @Id
    @Column(name = "queue_date", nullable = false, updatable = false)
    LocalDate queueDate;

    @Column(name = "last_number", nullable = false)
    Long lastNumber;

    @Version
    @Column(nullable = false)
    long version;
}
