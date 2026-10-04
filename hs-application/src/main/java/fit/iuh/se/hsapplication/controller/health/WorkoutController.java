package fit.iuh.se.hsapplication.controller.health;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hshealthrecord.dto.workout.*;
import fit.iuh.se.hshealthrecord.entity.workout.enums.ExerciseCategory;
import fit.iuh.se.hshealthrecord.service.workout.WorkoutService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkoutController {

    WorkoutService workoutService;

    @GetMapping("/exercises")
    public ApiResponse<List<ExerciseResponse>> getExercises(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(value = "category", required = false) ExerciseCategory category,
            @RequestParam(value = "query", required = false) String query) {
        return new ApiResponse<>(workoutService.getExercises(currentUser.getUserId(), category, query));
    }

    @PostMapping("/exercises")
    public ApiResponse<ExerciseResponse> createCustomExercise(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @Valid @RequestBody CreateCustomExerciseRequest request) {
        return new ApiResponse<>(workoutService.createCustomExercise(currentUser.getUserId(), request));
    }

    @DeleteMapping("/exercises/{id}")
    public ApiResponse<Void> deleteCustomExercise(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long id) {
        workoutService.deleteCustomExercise(currentUser.getUserId(), id);
        return new ApiResponse<>();
    }

    @GetMapping("/favorites")
    public ApiResponse<List<String>> getFavorites(
            @AuthenticationPrincipal UserAuthentication currentUser) {
        return new ApiResponse<>(workoutService.getFavoriteExerciseCodes(currentUser.getUserId()));
    }

    @PutMapping("/favorites/{exerciseCode}")
    public ApiResponse<Boolean> toggleFavorite(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable String exerciseCode) {
        return new ApiResponse<>(workoutService.toggleFavoriteExercise(currentUser.getUserId(), exerciseCode));
    }

    @GetMapping("/routines")
    public ApiResponse<List<WorkoutRoutineResponse>> getRoutines(
            @AuthenticationPrincipal UserAuthentication currentUser) {
        return new ApiResponse<>(workoutService.getRoutines(currentUser.getUserId()));
    }

    @PostMapping("/routines")
    public ApiResponse<WorkoutRoutineResponse> createRoutine(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @Valid @RequestBody CreateRoutineRequest request) {
        return new ApiResponse<>(workoutService.createRoutine(currentUser.getUserId(), request));
    }

    @DeleteMapping("/routines/{id}")
    public ApiResponse<Void> deleteRoutine(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long id) {
        workoutService.deleteRoutine(currentUser.getUserId(), id);
        return new ApiResponse<>();
    }

    @PostMapping("/sessions")
    public ApiResponse<WorkoutSessionResponse> saveSession(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @Valid @RequestBody CreateWorkoutSessionRequest request) {
        return new ApiResponse<>(workoutService.saveWorkoutSession(currentUser.getUserId(), request));
    }

    @GetMapping("/sessions")
    public ApiResponse<PageResponse<WorkoutSessionResponse>> getSessions(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startedAt").descending());
        return new ApiResponse<>(workoutService.getWorkoutSessions(currentUser.getUserId(), from, to, pageable));
    }

    @GetMapping("/stats/weekly")
    public ApiResponse<WeeklyWorkoutStatsResponse> getWeeklyStats(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(value = "referenceDate", required = false) String referenceDate,
            @RequestParam(value = "timezone", required = false) String timezone) {
        return new ApiResponse<>(workoutService.getWeeklyStats(currentUser.getUserId(), referenceDate, timezone));
    }

    @GetMapping("/stats/daily")
    public ApiResponse<DailyActivityResponse> getDailyActivity(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "timezone", required = false) String timezone) {
        return new ApiResponse<>(workoutService.getDailyActivity(currentUser.getUserId(), date, timezone));
    }

    @GetMapping(value = "/sessions/{id}/gpx", produces = "application/gpx+xml")
    public org.springframework.http.ResponseEntity<byte[]> exportGpx(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long id) {
        byte[] gpxBytes = workoutService.generateGpxFile(currentUser.getUserId(), id);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"workout_session_" + id + ".gpx\"")
                .body(gpxBytes);
    }

    @PostMapping("/steps/sync")
    public ApiResponse<DailyStepDetailResponse> syncStepData(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @Valid @RequestBody SyncStepDataRequest request) {
        return new ApiResponse<>(workoutService.syncStepData(currentUser.getUserId(), request));
    }

    @GetMapping("/steps/daily")
    public ApiResponse<DailyStepDetailResponse> getDailyStepDetail(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "timezone", required = false) String timezone) {
        return new ApiResponse<>(workoutService.getDailyStepDetail(currentUser.getUserId(), date, timezone));
    }

    @GetMapping("/steps/history")
    public ApiResponse<StepHistoryResponse> getStepHistory(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(value = "offset", defaultValue = "0") Integer dayOffset,
            @RequestParam(value = "timezone", required = false) String timezone) {
        return new ApiResponse<>(workoutService.getStepHistory(currentUser.getUserId(), dayOffset, timezone));
    }

    @PutMapping("/steps/goal")
    public ApiResponse<Void> updateStepGoal(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @Valid @RequestBody UpdateStepGoalRequest request) {
        workoutService.updateStepGoal(currentUser.getUserId(), request.getTargetSteps());
        return new ApiResponse<>();
    }
}
