package fit.iuh.se.hshealthrecord.service.workout.impl;

import fit.iuh.se.hsshared.i18n.RequestLanguage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Chữ của phần tập luyện theo header lang; bài tập người dùng tự tạo giữ nguyên tên. */
class WorkoutTextTest {
    private static void language(String lang) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestLanguage.HEADER, lang);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void clear() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void systemExercisesAndLabelsFollowTheRequestLanguage() {
        LocalDate monday = LocalDate.of(2026, 9, 28);
        LocalDate sunday = LocalDate.of(2026, 10, 4);

        language("vi");
        assertEquals("Đi bộ", WorkoutText.exerciseName("walking", true, "Đi bộ"));
        assertEquals("CN", WorkoutText.dayLabel(6));
        assertEquals("Ngày 28 - Ngày 4 tháng 10", WorkoutText.weekRange(monday, sunday));

        language("en");
        assertEquals("Walking", WorkoutText.exerciseName("walking", true, "Đi bộ"));
        assertEquals("Sun", WorkoutText.dayLabel(6));
        assertEquals("Sep 28 - Oct 4", WorkoutText.weekRange(monday, sunday));
        assertEquals("Walking", WorkoutText.sessionExerciseName("walking", "Đi bộ"));
        // Bài tự tạo / tên tự đặt: giữ nguyên
        assertEquals("Đi bộ buổi sáng", WorkoutText.exerciseName("walking", false, "Đi bộ buổi sáng"));
        assertEquals("Chạy quanh hồ", WorkoutText.sessionExerciseName("running", "Chạy quanh hồ"));
    }
}
