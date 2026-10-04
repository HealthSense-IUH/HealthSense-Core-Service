package fit.iuh.se.hshealthrecord.repository.workout;

import fit.iuh.se.hshealthrecord.entity.workout.UserDailyStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDailyStepRepository extends JpaRepository<UserDailyStep, Long> {

    Optional<UserDailyStep> findByUserIdAndStepDate(Long userId, LocalDate stepDate);

    List<UserDailyStep> findByUserIdAndStepDateBetweenOrderByStepDateAsc(
            Long userId, LocalDate fromDate, LocalDate toDate
    );

    @Query("SELECT s FROM UserDailyStep s WHERE s.userId = :userId AND s.stepDate >= :sinceDate ORDER BY s.stepDate ASC")
    List<UserDailyStep> findRecentSteps(
            @Param("userId") Long userId,
            @Param("sinceDate") LocalDate sinceDate
    );

    @Query("SELECT AVG(s.totalSteps) FROM UserDailyStep s WHERE s.stepDate >= :sinceDate")
    Double getCommunityAverageSteps(@Param("sinceDate") LocalDate sinceDate);
}
