package fit.iuh.se.hshealthrecord.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(
    name = "health_statistics_daily",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "stat_date"})
    }
)
@Getter
@Setter
public class DailyHealthStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "stat_date")
    private Instant statDate;

    @Column(name = "total_records")
    private Long totalRecords;

    @Column(name = "total_normal")
    private Integer totalNormal;

    @Column(name = "total_afib")
    private Integer totalAfib;

    @Column(name = "total_uncertain")
    private Integer totalUncertain;
}
