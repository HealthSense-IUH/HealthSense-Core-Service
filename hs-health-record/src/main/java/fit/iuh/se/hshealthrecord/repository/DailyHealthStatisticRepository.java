package fit.iuh.se.hshealthrecord.repository;

import fit.iuh.se.hshealthrecord.entity.DailyHealthStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DailyHealthStatisticRepository extends JpaRepository<DailyHealthStatistic, Long> {

    @Query("SELECT v.statDate as statDate, SUM(v.totalRecords) as totalRecords, " +
            "SUM(v.totalNormal) as totalNormal, SUM(v.totalAfib) as totalAfib, SUM(v.totalUncertain) as totalUncertain " +
            "FROM DailyHealthStatistic v " +
            "WHERE v.statDate >= :from AND v.statDate <= :to " +
            "GROUP BY v.statDate ORDER BY v.statDate ASC")
    List<Object[]> getSystemWideStats(@Param("from") Instant from, @Param("to") Instant to);
}
