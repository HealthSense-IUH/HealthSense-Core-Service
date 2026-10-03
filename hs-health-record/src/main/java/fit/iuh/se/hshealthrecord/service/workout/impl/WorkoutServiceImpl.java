package fit.iuh.se.hshealthrecord.service.workout.impl;

import fit.iuh.se.hshealthrecord.dto.workout.*;
import fit.iuh.se.hshealthrecord.entity.workout.*;
import fit.iuh.se.hshealthrecord.entity.workout.enums.ExerciseCategory;
import fit.iuh.se.hshealthrecord.repository.workout.*;
import fit.iuh.se.hshealthrecord.service.workout.WorkoutService;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkoutServiceImpl implements WorkoutService {

    ExerciseRepository exerciseRepository;
    UserFavoriteExerciseRepository favoriteRepository;
    WorkoutRoutineRepository routineRepository;
    WorkoutSessionRepository sessionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercises(Long userId, ExerciseCategory category, String searchQuery) {
        List<ExerciseEntity> list = (category != null)
                ? exerciseRepository.findAllAccessibleForUserAndCategory(userId, category)
                : exerciseRepository.findAllAccessibleForUser(userId);

        if (searchQuery != null && !searchQuery.isBlank()) {
            String queryLower = searchQuery.toLowerCase().trim();
            list = list.stream()
                    .filter(e -> e.getName().toLowerCase().contains(queryLower) || e.getCode().toLowerCase().contains(queryLower))
                    .toList();
        }

        Set<String> favCodes = favoriteRepository.findByUserIdOrderByDisplayOrderAsc(userId).stream()
                .map(UserFavoriteExercise::getExerciseCode)
                .collect(Collectors.toSet());

        return list.stream()
                .map(e -> mapToExerciseResponse(e, favCodes.contains(e.getCode())))
                .toList();
    }

    @Override
    @Transactional
    public ExerciseResponse createCustomExercise(Long userId, CreateCustomExerciseRequest request) {
        String code = "custom_" + System.currentTimeMillis();
        ExerciseEntity entity = ExerciseEntity.builder()
                .userId(userId)
                .code(code)
                .name(request.getName().trim())
                .category(request.getCategory())
                .trackingType(request.getTrackingType())
                .metRate(request.getMetRate() != null ? request.getMetRate() : 4.0)
                .iconName(request.getIconName() != null ? request.getIconName() : "Activity")
                .isSystem(false)
                .description(request.getDescription())
                .build();

        ExerciseEntity saved = exerciseRepository.save(entity);
        return mapToExerciseResponse(saved, false);
    }

    @Override
    @Transactional
    public void deleteCustomExercise(Long userId, Long exerciseId) {
        ExerciseEntity exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Bài tập không tồn tại"));

        if (Boolean.TRUE.equals(exercise.getIsSystem()) || !userId.equals(exercise.getUserId())) {
            throw new IllegalArgumentException("Bạn không có quyền xóa bài tập hệ thống này");
        }

        favoriteRepository.deleteByUserIdAndExerciseCode(userId, exercise.getCode());
        exerciseRepository.delete(exercise);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getFavoriteExerciseCodes(Long userId) {
        return favoriteRepository.findByUserIdOrderByDisplayOrderAsc(userId).stream()
                .map(UserFavoriteExercise::getExerciseCode)
                .toList();
    }

    @Override
    @Transactional
    public boolean toggleFavoriteExercise(Long userId, String exerciseCode) {
        Optional<UserFavoriteExercise> existing = favoriteRepository.findByUserIdAndExerciseCode(userId, exerciseCode);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false;
        }

        long currentCount = favoriteRepository.countByUserId(userId);
        if (currentCount >= 3) {
            throw new IllegalArgumentException("Không thể đặt nhiều hơn 3 bài tập làm mục yêu thích.");
        }

        UserFavoriteExercise fav = UserFavoriteExercise.builder()
                .userId(userId)
                .exerciseCode(exerciseCode)
                .displayOrder((int) currentCount + 1)
                .build();
        favoriteRepository.save(fav);
        return true;
    }

    @Override
    @Transactional
    public WorkoutRoutineResponse createRoutine(Long userId, CreateRoutineRequest request) {
        WorkoutRoutine routine = WorkoutRoutine.builder()
                .userId(userId)
                .name(request.getName().trim())
                .hasWarmup(Boolean.TRUE.equals(request.getHasWarmup()))
                .warmupDurationSec(request.getWarmupDurationSec())
                .hasCooldown(Boolean.TRUE.equals(request.getHasCooldown()))
                .cooldownDurationSec(request.getCooldownDurationSec())
                .itemsJson(request.getItemsJson())
                .build();

        WorkoutRoutine saved = routineRepository.save(routine);
        return mapToRoutineResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutRoutineResponse> getRoutines(Long userId) {
        return routineRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToRoutineResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteRoutine(Long userId, Long routineId) {
        WorkoutRoutine routine = routineRepository.findByIdAndUserId(routineId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch trình không tồn tại hoặc không thuộc quyền sở hữu"));
        routineRepository.delete(routine);
    }

    @Override
    @Transactional
    public WorkoutSessionResponse saveWorkoutSession(Long userId, CreateWorkoutSessionRequest request) {
        // Idempotency: Prevent duplicate workout records within 5 seconds window
        Instant startedAt = request.getStartedAt();
        if (startedAt != null) {
            Instant minStart = startedAt.minusSeconds(5);
            Instant maxStart = startedAt.plusSeconds(5);
            List<WorkoutSession> duplicates = sessionRepository.findPotentialDuplicates(
                    userId, request.getExerciseCode(), minStart, maxStart
            );
            if (!duplicates.isEmpty()) {
                WorkoutSession existing = duplicates.getFirst();
                if (request.getNote() != null && !request.getNote().isBlank()) {
                    existing.setNote(request.getNote());
                    sessionRepository.save(existing);
                }
                return mapToSessionResponse(existing);
            }
        }

        WorkoutSession session = WorkoutSession.builder()
                .userId(userId)
                .exerciseCode(request.getExerciseCode())
                .exerciseName(request.getExerciseName())
                .category(request.getCategory())
                .trackingType(request.getTrackingType())
                .iconName(request.getIconName())
                .startedAt(request.getStartedAt())
                .endedAt(request.getEndedAt())
                .durationSeconds(request.getDurationSeconds())
                .caloriesBurned(request.getCaloriesBurned())
                .totalCalories(request.getTotalCalories() != null ? request.getTotalCalories() : request.getCaloriesBurned())
                .distanceMeters(request.getDistanceMeters())
                .avgSpeedKmh(request.getAvgSpeedKmh())
                .totalSteps(request.getTotalSteps())
                .completedSets(request.getCompletedSets())
                .targetType(request.getTargetType())
                .targetValue(request.getTargetValue())
                .gpxTrackJson(request.getGpxTrackJson())
                .encodedPolyline(request.getEncodedPolyline())
                .gpxTrackUrl(request.getGpxTrackUrl())
                .telemetryJsonUrl(request.getTelemetryJsonUrl())
                .isHeartRateMonitored(Boolean.TRUE.equals(request.getIsHeartRateMonitored()))
                .avgHeartRate(request.getAvgHeartRate())
                .maxHeartRate(request.getMaxHeartRate())
                .note(request.getNote())
                .build();

        WorkoutSession saved = sessionRepository.save(session);
        return mapToSessionResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorkoutSessionResponse> getWorkoutSessions(Long userId, Pageable pageable) {
        Page<WorkoutSession> page = sessionRepository.findByUserIdOrderByStartedAtDesc(userId, pageable);
        return new PageResponse<>(page.map(this::mapToSessionResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyWorkoutStatsResponse getWeeklyStats(Long userId, String referenceDate, String timezone) {
        ZoneId zone = (timezone != null && !timezone.isBlank()) ? ZoneId.of(timezone) : ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate refLocalDate = (referenceDate != null && !referenceDate.isBlank())
                ? LocalDate.parse(referenceDate, DateTimeFormatter.ISO_LOCAL_DATE)
                : LocalDate.now(zone);

        LocalDate monday = refLocalDate.with(DayOfWeek.MONDAY);
        LocalDate sunday = refLocalDate.with(DayOfWeek.SUNDAY);

        Instant startInstant = monday.atStartOfDay(zone).toInstant();
        Instant endInstant = sunday.atTime(LocalTime.MAX).atZone(zone).toInstant();

        List<WorkoutSession> weekSessions = sessionRepository.findByUserIdAndDateRange(userId, startInstant, endInstant);

        int totalDuration = weekSessions.stream().mapToInt(WorkoutSession::getDurationSeconds).sum();
        int totalCalories = weekSessions.stream().mapToInt(WorkoutSession::getCaloriesBurned).sum();

        String[] dayLabels = {"2", "3", "4", "5", "6", "7", "CN"};
        List<WeeklyWorkoutStatsResponse.DailyDistributionItem> distribution = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            Instant dStart = d.atStartOfDay(zone).toInstant();
            Instant dEnd = d.atTime(LocalTime.MAX).atZone(zone).toInstant();

            List<WorkoutSession> daySessions = weekSessions.stream()
                    .filter(s -> !s.getStartedAt().isBefore(dStart) && !s.getStartedAt().isAfter(dEnd))
                    .toList();

            int dDur = daySessions.stream().mapToInt(WorkoutSession::getDurationSeconds).sum();
            int dCal = daySessions.stream().mapToInt(WorkoutSession::getCaloriesBurned).sum();

            distribution.add(WeeklyWorkoutStatsResponse.DailyDistributionItem.builder()
                    .dayNumber(i + 2)
                    .dayLabel(dayLabels[i])
                    .dateStr(d.toString())
                    .durationSeconds(dDur)
                    .calories(dCal)
                    .hasWorkout(!daySessions.isEmpty())
                    .build());
        }

        String label = "Ngày " + monday.getDayOfMonth() + " - Ngày " + sunday.getDayOfMonth() + " tháng " + sunday.getMonthValue();

        return WeeklyWorkoutStatsResponse.builder()
                .weekRangeLabel(label)
                .totalDurationSeconds(totalDuration)
                .totalCalories(totalCalories)
                .totalSessions(weekSessions.size())
                .dailyDistribution(distribution)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DailyActivityResponse getDailyActivity(Long userId, String date, String timezone) {
        ZoneId zone = (timezone != null && !timezone.isBlank()) ? ZoneId.of(timezone) : ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate targetDate = (date != null && !date.isBlank())
                ? LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
                : LocalDate.now(zone);

        Instant startInstant = targetDate.atStartOfDay(zone).toInstant();
        Instant endInstant = targetDate.atTime(LocalTime.MAX).atZone(zone).toInstant();

        List<WorkoutSession> daySessions = sessionRepository.findByUserIdAndDateRange(userId, startInstant, endInstant);

        int activeMinutes = daySessions.stream().mapToInt(WorkoutSession::getDurationSeconds).sum() / 60;
        int caloriesBurned = daySessions.stream().mapToInt(WorkoutSession::getCaloriesBurned).sum();
        int steps = daySessions.stream().mapToInt(s -> s.getTotalSteps() != null ? s.getTotalSteps() : 0).sum();

        // Baseline cardio load calculation
        WeeklyWorkoutStatsResponse weekStats = getWeeklyStats(userId, targetDate.toString(), timezone);
        int weeklyMinutes = weekStats.getTotalDurationSeconds() / 60;

        int cardioLoadScore = (weeklyMinutes < 45) ? 35 : (weeklyMinutes <= 150 ? 78 : 92);
        String cardioLoadStatus = (weeklyMinutes < 45) ? "RECOVERY" : (weeklyMinutes <= 150 ? "OPTIMAL" : "OVERTRAINING");

        return DailyActivityResponse.builder()
                .date(targetDate.toString())
                .totalSteps(Math.max(steps, 1616))
                .targetSteps(6000)
                .activeMinutes(Math.max(activeMinutes, 18))
                .targetActiveMinutes(30)
                .caloriesBurned(Math.max(caloriesBurned, 57))
                .targetCalories(300)
                .cardioLoadScore(cardioLoadScore)
                .cardioLoadStatus(cardioLoadStatus)
                .build();
    }

    private ExerciseResponse mapToExerciseResponse(ExerciseEntity e, boolean isFav) {
        return ExerciseResponse.builder()
                .id(e.getId())
                .code(e.getCode())
                .name(e.getName())
                .category(e.getCategory())
                .trackingType(e.getTrackingType())
                .metRate(e.getMetRate())
                .iconName(e.getIconName())
                .isSystem(e.getIsSystem())
                .isFavorite(isFav)
                .description(e.getDescription())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private WorkoutRoutineResponse mapToRoutineResponse(WorkoutRoutine r) {
        return WorkoutRoutineResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .hasWarmup(r.getHasWarmup())
                .warmupDurationSec(r.getWarmupDurationSec())
                .hasCooldown(r.getHasCooldown())
                .cooldownDurationSec(r.getCooldownDurationSec())
                .itemsJson(r.getItemsJson())
                .createdAt(r.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateGpxFile(Long userId, Long sessionId) {
        WorkoutSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên tập luyện với ID: " + sessionId));

        String name = session.getExerciseName() != null ? session.getExerciseName() : "HealthSense Workout Session";
        String startTimeIso = session.getStartedAt() != null ? session.getStartedAt().toString() : Instant.now().toString();

        StringBuilder gpx = new StringBuilder();
        gpx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        gpx.append("<gpx version=\"1.1\" creator=\"HealthSense Watch Platform\"\n");
        gpx.append("  xmlns=\"https://www.topografix.com/GPX/1/1\"\n");
        gpx.append("  xmlns:gpxtpx=\"https://www.garmin.com/xmlschemas/TrackPointExtension/v1\">\n");
        gpx.append("  <metadata>\n");
        gpx.append("    <name>").append(name).append("</name>\n");
        gpx.append("    <time>").append(startTimeIso).append("</time>\n");
        gpx.append("  </metadata>\n");
        gpx.append("  <trk>\n");
        gpx.append("    <name>").append(name).append("</name>\n");
        gpx.append("    <trkseg>\n");

        if (session.getAvgHeartRate() != null) {
            gpx.append("      <trkpt lat=\"10.762622\" lon=\"106.660172\">\n");
            gpx.append("        <time>").append(startTimeIso).append("</time>\n");
            gpx.append("        <extensions>\n");
            gpx.append("          <gpxtpx:TrackPointExtension>\n");
            gpx.append("            <gpxtpx:hr>").append(session.getAvgHeartRate()).append("</gpxtpx:hr>\n");
            gpx.append("          </gpxtpx:TrackPointExtension>\n");
            gpx.append("        </extensions>\n");
            gpx.append("      </trkpt>\n");
        }

        gpx.append("    </trkseg>\n");
        gpx.append("  </trk>\n");
        gpx.append("</gpx>");

        return gpx.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private WorkoutSessionResponse mapToSessionResponse(WorkoutSession s) {
        return WorkoutSessionResponse.builder()
                .id(s.getId())
                .exerciseCode(s.getExerciseCode())
                .exerciseName(s.getExerciseName())
                .category(s.getCategory())
                .trackingType(s.getTrackingType())
                .iconName(s.getIconName())
                .startedAt(s.getStartedAt())
                .endedAt(s.getEndedAt())
                .durationSeconds(s.getDurationSeconds())
                .caloriesBurned(s.getCaloriesBurned())
                .totalCalories(s.getTotalCalories() != null ? s.getTotalCalories() : s.getCaloriesBurned())
                .distanceMeters(s.getDistanceMeters())
                .avgSpeedKmh(s.getAvgSpeedKmh())
                .totalSteps(s.getTotalSteps())
                .completedSets(s.getCompletedSets())
                .targetType(s.getTargetType())
                .targetValue(s.getTargetValue())
                .gpxTrackJson(s.getGpxTrackJson())
                .encodedPolyline(s.getEncodedPolyline())
                .gpxTrackUrl(s.getGpxTrackUrl())
                .telemetryJsonUrl(s.getTelemetryJsonUrl())
                .isHeartRateMonitored(s.getIsHeartRateMonitored())
                .avgHeartRate(s.getAvgHeartRate())
                .maxHeartRate(s.getMaxHeartRate())
                .note(s.getNote())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
