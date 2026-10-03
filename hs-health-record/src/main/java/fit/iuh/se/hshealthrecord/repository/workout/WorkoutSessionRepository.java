package fit.iuh.se.hshealthrecord.repository.workout;

import fit.iuh.se.hshealthrecord.entity.workout.WorkoutSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    Page<WorkoutSession> findByUserIdOrderByStartedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT s FROM WorkoutSession s WHERE s.userId = :userId AND s.startedAt >= :startTime AND s.startedAt <= :endTime ORDER BY s.startedAt ASC")
    List<WorkoutSession> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );

    @Query("SELECT s FROM WorkoutSession s WHERE s.userId = :userId AND s.exerciseCode = :exerciseCode ORDER BY s.startedAt DESC")
    List<WorkoutSession> findByUserIdAndExerciseCode(
            @Param("userId") Long userId,
            @Param("exerciseCode") String exerciseCode
    );

    @Query("SELECT s FROM WorkoutSession s WHERE s.userId = :userId AND s.exerciseCode = :exerciseCode AND s.startedAt >= :minStart AND s.startedAt <= :maxStart ORDER BY s.startedAt DESC")
    List<WorkoutSession> findPotentialDuplicates(
            @Param("userId") Long userId,
            @Param("exerciseCode") String exerciseCode,
            @Param("minStart") Instant minStart,
            @Param("maxStart") Instant maxStart
    );

    Optional<WorkoutSession> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}
