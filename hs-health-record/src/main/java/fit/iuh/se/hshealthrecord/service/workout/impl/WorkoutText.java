package fit.iuh.se.hshealthrecord.service.workout.impl;

import fit.iuh.se.hsshared.i18n.RequestLanguage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Chữ hiển thị của phần tập luyện theo ngôn ngữ của request (header {@code lang}): tên / mô tả bài tập hệ thống (seed
 * tiếng Việt, khóa theo {@code code}), nhãn thứ trong tuần và nhãn khoảng tuần. Bài tập người dùng tự tạo giữ nguyên tên.
 */
final class WorkoutText {
    private record Exercise(String vietnameseName, String englishName, String englishDescription) {
    }

    /** Danh mục 18 bài tập hệ thống chuẩn (khớp Mobile) + giữ mã cũ cho lịch sử phiên tập. */
    private static final Map<String, Exercise> SYSTEM_EXERCISES = Map.ofEntries(
            Map.entry("walking", new Exercise("Đi bộ", "Walking",
                    "Gentle outdoor walking that boosts circulation and heart health.")),
            Map.entry("indoor_walking", new Exercise("Đi bộ trong nhà", "Indoor walking",
                    "Indoor or treadmill walking without GPS, tracking steps and distance.")),
            Map.entry("yoga", new Exercise("Yoga", "Yoga",
                    "Regulates breathing, stretches the body, and reduces stress.")),
            Map.entry("stretching", new Exercise("Giãn cơ", "Stretching",
                    "Stretches the muscles, supports recovery, and improves joint flexibility.")),
            Map.entry("running", new Exercise("Chạy bộ", "Running",
                    "Outdoor running that builds cardiorespiratory endurance and burns energy.")),
            Map.entry("cycling", new Exercise("Đạp xe", "Cycling",
                    "Outdoor cycling that trains leg endurance and circulation with low knee impact.")),
            Map.entry("treadmill", new Exercise("Máy chạy bộ", "Treadmill",
                    "Indoor treadmill running with controlled pace and heart rate.")),
            Map.entry("indoor_cycling", new Exercise("Đạp xe trong nhà", "Indoor cycling",
                    "Stationary indoor cycling for safe heart-rate-controlled cardio.")),
            Map.entry("jump_rope", new Exercise("Nhảy dây", "Jump rope",
                    "High-intensity cardio that improves rhythm and burns calories quickly.")),
            Map.entry("push_up", new Exercise("Hít đất", "Push-ups",
                    "Builds chest, shoulder, and triceps strength using body weight.")),
            Map.entry("squats", new Exercise("Squats", "Squats",
                    "Strengthens thighs and glutes while improving lower-body balance.")),
            Map.entry("plank", new Exercise("Plank", "Plank",
                    "Isometric core hold that strengthens abs and stabilizes the spine.")),
            Map.entry("crunches", new Exercise("Gập bụng", "Crunches",
                    "Targets the upper abdominal muscles and core stability.")),
            Map.entry("dumbbell_curl", new Exercise("Tập tạ tay", "Dumbbell curls",
                    "Builds arm and upper-body strength with manageable dumbbells.")),
            Map.entry("pool_swimming", new Exercise("Bơi lội", "Swimming",
                    "Full-body swimming that builds lung capacity while protecting joints.")),
            Map.entry("badminton", new Exercise("Cầu lông", "Badminton",
                    "Popular racket sport that builds reflexes and agility.")),
            Map.entry("soccer", new Exercise("Bóng đá", "Soccer",
                    "High-energy team sport that improves cardiovascular endurance.")),
            Map.entry("pickleball", new Exercise("Pickleball", "Pickleball",
                    "Accessible, heart-friendly racket sport suitable for all ages.")),
            // Giữ tương thích dịch lịch sử phiên tập cũ
            Map.entry("swimming", new Exercise("Bơi lội", "Swimming",
                    "Full-body swimming that builds lung capacity while protecting joints.")),
            Map.entry("combined_workout", new Exercise("Bài tập kết hợp", "Combined workout",
                    "A mix of physical exercises for endurance and the whole body.")),
            Map.entry("strength_training", new Exercise("Tập tạ", "Strength training",
                    "Builds strength and stimulates muscle growth and bone density.")));

    private static final String[] DAY_LABELS_VI = {"2", "3", "4", "5", "6", "7", "CN"};
    private static final String[] DAY_LABELS_EN = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private static final DateTimeFormatter MONTH_DAY_EN = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

    private WorkoutText() {}

    private static boolean english() {
        return !RequestLanguage.isVietnamese(RequestLanguage.current());
    }

    /** Tên bài tập: bài hệ thống thì theo ngôn ngữ, bài tự tạo giữ nguyên. */
    static String exerciseName(String code, boolean system, String name) {
        Exercise exercise = system && code != null ? SYSTEM_EXERCISES.get(code) : null;
        return exercise != null && english() ? exercise.englishName() : name;
    }

    static String exerciseDescription(String code, boolean system, String description) {
        Exercise exercise = system && code != null ? SYSTEM_EXERCISES.get(code) : null;
        return exercise != null && english() ? exercise.englishDescription() : description;
    }

    /** Tên bài tập lưu trong phiên tập (client gửi lúc tạo): đúng tên tiếng Việt của bài hệ thống thì dịch. */
    static String sessionExerciseName(String code, String name) {
        Exercise exercise = code == null ? null : SYSTEM_EXERCISES.get(code);
        boolean systemName = exercise != null && exercise.vietnameseName().equals(name);
        return systemName && english() ? exercise.englishName() : name;
    }

    /** Nhãn thứ, index 0 = thứ Hai. */
    static String dayLabel(int index) {
        return (english() ? DAY_LABELS_EN : DAY_LABELS_VI)[index];
    }

    /** "Ngày 6 - Ngày 12 tháng 10" / "Oct 6 - Oct 12". */
    static String weekRange(LocalDate monday, LocalDate sunday) {
        if (english()) return MONTH_DAY_EN.format(monday) + " - " + MONTH_DAY_EN.format(sunday);
        return "Ngày " + monday.getDayOfMonth() + " - Ngày " + sunday.getDayOfMonth() + " tháng " + sunday.getMonthValue();
    }

    /** "Khoảng thời gian năng động nhất: 07:00 - 08:00" / "Most active period: 07:00 - 08:00". */
    static String activePeriod(int peakHour) {
        String range = String.format(Locale.US, "%02d:00 - %02d:00", peakHour, peakHour + 1);
        return english()
                ? "Most active period: " + range
                : "Khoảng thời gian năng động nhất: " + range;
    }
}
