package fit.iuh.se.hshealthrecord.repository.workout;

import fit.iuh.se.hshealthrecord.entity.workout.UserFavoriteExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavoriteExerciseRepository extends JpaRepository<UserFavoriteExercise, Long> {

    List<UserFavoriteExercise> findByUserIdOrderByDisplayOrderAsc(Long userId);

    long countByUserId(Long userId);

    Optional<UserFavoriteExercise> findByUserIdAndExerciseCode(Long userId, String exerciseCode);

    void deleteByUserIdAndExerciseCode(Long userId, String exerciseCode);
}
