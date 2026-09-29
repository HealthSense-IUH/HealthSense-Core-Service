package fit.iuh.se.hshealthrecord.repository.workout;

import fit.iuh.se.hshealthrecord.entity.workout.ExerciseEntity;
import fit.iuh.se.hshealthrecord.entity.workout.enums.ExerciseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<ExerciseEntity, Long> {

    @Query("SELECT e FROM ExerciseEntity e WHERE e.userId IS NULL OR e.userId = :userId ORDER BY e.isSystem DESC, e.createdAt ASC")
    List<ExerciseEntity> findAllAccessibleForUser(@Param("userId") Long userId);

    @Query("SELECT e FROM ExerciseEntity e WHERE (e.userId IS NULL OR e.userId = :userId) AND e.category = :category ORDER BY e.isSystem DESC, e.createdAt ASC")
    List<ExerciseEntity> findAllAccessibleForUserAndCategory(@Param("userId") Long userId, @Param("category") ExerciseCategory category);

    Optional<ExerciseEntity> findByCode(String code);

    boolean existsByCode(String code);

    List<ExerciseEntity> findByUserId(Long userId);
}
