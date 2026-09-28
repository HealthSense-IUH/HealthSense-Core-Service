package fit.iuh.se.hshealthrecord.repository.workout;

import fit.iuh.se.hshealthrecord.entity.workout.WorkoutRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutRoutineRepository extends JpaRepository<WorkoutRoutine, Long> {

    List<WorkoutRoutine> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<WorkoutRoutine> findByIdAndUserId(Long id, Long userId);
}
