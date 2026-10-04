package fit.iuh.se.hshealthrecord.service.workout;

import fit.iuh.se.hshealthrecord.dto.workout.*;
import fit.iuh.se.hshealthrecord.entity.workout.enums.ExerciseCategory;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface WorkoutService {

    List<ExerciseResponse> getExercises(Long userId, ExerciseCategory category, String searchQuery);

    ExerciseResponse createCustomExercise(Long userId, CreateCustomExerciseRequest request);

    void deleteCustomExercise(Long userId, Long exerciseId);

    List<String> getFavoriteExerciseCodes(Long userId);

    boolean toggleFavoriteExercise(Long userId, String exerciseCode);

    WorkoutRoutineResponse createRoutine(Long userId, CreateRoutineRequest request);

    List<WorkoutRoutineResponse> getRoutines(Long userId);

    void deleteRoutine(Long userId, Long routineId);

    WorkoutSessionResponse saveWorkoutSession(Long userId, CreateWorkoutSessionRequest request);

    PageResponse<WorkoutSessionResponse> getWorkoutSessions(Long userId, Pageable pageable);

    PageResponse<WorkoutSessionResponse> getWorkoutSessions(Long userId, Instant from, Instant to, Pageable pageable);

    WeeklyWorkoutStatsResponse getWeeklyStats(Long userId, String referenceDate, String timezone);

    DailyActivityResponse getDailyActivity(Long userId, String date, String timezone);

    byte[] generateGpxFile(Long userId, Long sessionId);

    DailyStepDetailResponse syncStepData(Long userId, SyncStepDataRequest request);

    DailyStepDetailResponse getDailyStepDetail(Long userId, String date, String timezone);

    StepHistoryResponse getStepHistory(Long userId, Integer dayOffset, String timezone);

    void updateStepGoal(Long userId, Integer targetSteps);
}
