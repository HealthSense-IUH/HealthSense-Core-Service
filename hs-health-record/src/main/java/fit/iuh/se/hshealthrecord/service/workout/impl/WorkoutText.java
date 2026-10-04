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

    /** Bài tập hệ thống trong V20260928220000__create_workout_tables.sql. */
    private static final Map<String, Exercise> SYSTEM_EXERCISES = Map.of(
            "walking", new Exercise("Đi bộ", "Walking",
                    "Natural walking that boosts blood circulation and helps you relax."),
            "running", new Exercise("Chạy bộ", "Running",
                    "Running builds endurance and improves VO2 max and heart health."),
            "cycling", new Exercise("Đạp xe", "Cycling",
                    "Outdoor or stationary cycling that trains leg muscles and endurance."),
            "badminton", new Exercise("Cầu lông", "Badminton",
                    "A fast-reflex sport with constant movement that works the whole body."),
            "swimming", new Exercise("Bơi lội", "Swimming",
                    "Full-body swimming that builds muscle and lung capacity while easing joint stress."),
            "combined_workout", new Exercise("Bài tập kết hợp", "Combined workout",
                    "A mix of physical exercises for endurance and the whole body."),
            "stretching", new Exercise("Giãn cơ", "Stretching",
                    "Stretches the muscles, supports recovery and improves joint flexibility."),
            "yoga", new Exercise("Yoga", "Yoga",
                    "Focuses on breathing, stretching and balancing the body's energy."),
            "jump_rope", new Exercise("Nhảy dây", "Jump rope",
                    "High-intensity calorie burning that builds explosive power and rhythm."),
            "strength_training", new Exercise("Tập tạ", "Strength training",
                    "Builds strength and stimulates muscle growth and bone density."));

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
}
