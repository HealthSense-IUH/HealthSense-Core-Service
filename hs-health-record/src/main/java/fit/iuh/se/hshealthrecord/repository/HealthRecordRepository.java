package fit.iuh.se.hshealthrecord.repository;

import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long>, JpaSpecificationExecutor<HealthRecord> {

    Page<HealthRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<HealthRecord> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<HealthRecord> findByIdAndUserId(Long id, Long userId);

    List<HealthRecord> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long userId, Instant from, Instant to);

    long countByUserId(Long userId);

    @Query(value = "SELECT EXTRACT(HOUR FROM (created_at AT TIME ZONE :timezone)) AS statGroup, " +
            "CAST(SUM(CASE WHEN prediction_label = 'NORMAL' THEN 1 ELSE 0 END) AS INTEGER) AS normalCount, " +
            "CAST(SUM(CASE WHEN prediction_label = 'AFIB' THEN 1 ELSE 0 END) AS INTEGER) AS afibRiskCount, " +
            "CAST(SUM(CASE WHEN prediction_label = 'UNCERTAIN' THEN 1 ELSE 0 END) AS INTEGER) AS uncertainCount, " +
            "CAST(SUM(CASE WHEN prediction_label = 'AFIB_SUSPECTED' THEN 1 ELSE 0 END) AS INTEGER) AS afibSuspectedCount " +
            "FROM health_records " +
            "WHERE user_id = :userId AND status = 'COMPLETED' AND created_at BETWEEN :from AND :to " +
            "GROUP BY 1", nativeQuery = true)
    List<HealthStatProjection> getStatsByDay(@Param("userId") Long userId,
                                             @Param("from") Instant from,
                                             @Param("to") Instant to,
                                             @Param("timezone") String timezone);

    @Query(value = "SELECT EXTRACT(ISODOW FROM (created_at AT TIME ZONE :timezone)) AS statGroup, " +
            "CAST(SUM(CASE WHEN prediction_label = 'NORMAL' THEN 1 ELSE 0 END) AS INTEGER) AS normalCount, " +
            "CAST(SUM(CASE WHEN prediction_label = 'AFIB' THEN 1 ELSE 0 END) AS INTEGER) AS afibRiskCount, " +
            "CAST(SUM(CASE WHEN prediction_label = 'UNCERTAIN' THEN 1 ELSE 0 END) AS INTEGER) AS uncertainCount, " +
            "CAST(SUM(CASE WHEN prediction_label = 'AFIB_SUSPECTED' THEN 1 ELSE 0 END) AS INTEGER) AS afibSuspectedCount " +
            "FROM health_records " +
            "WHERE user_id = :userId AND status = 'COMPLETED' AND created_at BETWEEN :from AND :to " +
            "GROUP BY 1", nativeQuery = true)
    List<HealthStatProjection> getStatsByWeek(@Param("userId") Long userId,
                                              @Param("from") Instant from,
                                              @Param("to") Instant to,
                                              @Param("timezone") String timezone);

    @Query(value = "SELECT EXTRACT(DAY FROM (created_at AT TIME ZONE :timezone)) AS statGroup, " +
            "CAST(SUM(CASE WHEN prediction_label = 'NORMAL' THEN 1 ELSE 0 END) AS INTEGER) AS normalCount, " +
            "CAST(SUM(CASE WHEN prediction_label = 'AFIB' THEN 1 ELSE 0 END) AS INTEGER) AS afibRiskCount, " +
            "CAST(SUM(CASE WHEN prediction_label = 'UNCERTAIN' THEN 1 ELSE 0 END) AS INTEGER) AS uncertainCount, " +
            "CAST(SUM(CASE WHEN prediction_label = 'AFIB_SUSPECTED' THEN 1 ELSE 0 END) AS INTEGER) AS afibSuspectedCount " +
            "FROM health_records " +
            "WHERE user_id = :userId AND status = 'COMPLETED' AND created_at BETWEEN :from AND :to " +
            "GROUP BY 1", nativeQuery = true)
    List<HealthStatProjection> getStatsByMonth(@Param("userId") Long userId,
                                               @Param("from") Instant from,
                                               @Param("to") Instant to,
                                               @Param("timezone") String timezone);

    @Query(value = "SELECT EXTRACT(MONTH FROM (created_at AT TIME ZONE :timezone)) AS statGroup, " +
            "CAST(SUM(CASE WHEN prediction_label = 'NORMAL' THEN 1 ELSE 0 END) AS INTEGER) AS normalCount, " +
            "CAST(SUM(CASE WHEN prediction_label = 'AFIB' THEN 1 ELSE 0 END) AS INTEGER) AS afibRiskCount, " +
            "CAST(SUM(CASE WHEN prediction_label = 'UNCERTAIN' THEN 1 ELSE 0 END) AS INTEGER) AS uncertainCount, " +
            "CAST(SUM(CASE WHEN prediction_label = 'AFIB_SUSPECTED' THEN 1 ELSE 0 END) AS INTEGER) AS afibSuspectedCount " +
            "FROM health_records " +
            "WHERE user_id = :userId AND status = 'COMPLETED' AND created_at BETWEEN :from AND :to " +
            "GROUP BY 1", nativeQuery = true)
    List<HealthStatProjection> getStatsByYear(@Param("userId") Long userId,
                                              @Param("from") Instant from,
                                              @Param("to") Instant to,
                                              @Param("timezone") String timezone);
}
