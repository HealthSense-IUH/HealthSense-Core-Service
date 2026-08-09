package fit.iuh.se.hschat.service.doctor.impl;

import fit.iuh.se.hschat.service.doctor.SupportScheduleValidator;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
public class DeclaredSupportScheduleValidator implements SupportScheduleValidator {

    @Override
    public void validate(String availabilityJson, String timezone, boolean required) {
        if (isBlank(timezone))
            throw invalidSchedule();
        try {
            ZoneId.of(timezone);
        } catch (RuntimeException exception) {
            throw invalidSchedule();
        }

        if (isBlank(availabilityJson)) {
            if (required)
                throw invalidSchedule();
            return;
        }

        Document root;
        try {
            root = Document.parse(availabilityJson);
        } catch (RuntimeException exception) {
            throw invalidSchedule();
        }

        Object weeklyObject = root.get("weekly");
        if (!(weeklyObject instanceof List<?> weekly) || weekly.isEmpty()) {
            if (required)
                throw invalidSchedule();
            return;
        }

        Map<DayOfWeek, List<TimeRange>> rangesByDay = new EnumMap<>(DayOfWeek.class);
        for (Object item : weekly) {
            if (!(item instanceof Document document))
                throw invalidSchedule();

            DayOfWeek day = parseDay(document.getString("dayOfWeek"));
            LocalTime start = parseTime(document.getString("start"));
            LocalTime end = parseTime(document.getString("end"));
            if (!start.isBefore(end))
                throw invalidSchedule();

            rangesByDay.computeIfAbsent(day, ignored -> new ArrayList<>()).add(new TimeRange(start, end));
        }

        rangesByDay.values().forEach(this::ensureNoOverlap);
    }

    @Override
    public boolean isValid(String availabilityJson, String timezone, boolean required) {
        try {
            validate(availabilityJson, timezone, required);
            return true;
        } catch (AppException exception) {
            return false;
        }
    }

    private DayOfWeek parseDay(String value) {
        try {
            return DayOfWeek.valueOf(value);
        } catch (RuntimeException exception) {
            throw invalidSchedule();
        }
    }

    private LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw invalidSchedule();
        }
    }

    private void ensureNoOverlap(List<TimeRange> ranges) {
        ranges.sort(Comparator.comparing(TimeRange::start));
        for (int index = 1; index < ranges.size(); index++) {
            TimeRange previous = ranges.get(index - 1);
            TimeRange current = ranges.get(index);
            if (current.start().isBefore(previous.end()))
                throw invalidSchedule();
        }
    }

    private AppException invalidSchedule() {
        return new AppException(ErrorCode.INVALID_DOCTOR_SUPPORT_SCHEDULE);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record TimeRange(LocalTime start, LocalTime end) {
    }
}
