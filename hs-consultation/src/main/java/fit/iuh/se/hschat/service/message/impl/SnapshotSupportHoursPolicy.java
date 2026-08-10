package fit.iuh.se.hschat.service.message.impl;

import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationParticipantRole;
import fit.iuh.se.hschat.service.message.SupportHoursPolicy;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;

@Component
@Slf4j
public class SnapshotSupportHoursPolicy implements SupportHoursPolicy {

    @Override
    public boolean canSendNow(ConsultationSession session, ConsultationParticipantRole role) {
        if (role != ConsultationParticipantRole.MEMBER)
            return true;

        String scheduleJson = session.getSupportScheduleSnapshotJson();
        String timezone = session.getSupportTimezoneSnapshot();
        if (isBlank(scheduleJson) || isBlank(timezone)) {
            log.warn("Consultation session {} has no support schedule snapshot; allowing legacy member message", session.getId());
            return true;
        }

        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timezone));
            Document root = Document.parse(scheduleJson);
            List<Document> weekly = root.getList("weekly", Document.class);
            if (weekly == null || weekly.isEmpty())
                return false;

            String today = now.getDayOfWeek().name();
            LocalTime currentTime = now.toLocalTime();
            return weekly.stream()
                    .filter(slot -> today.equalsIgnoreCase(slot.getString("dayOfWeek")))
                    .anyMatch(slot -> isWithinSlot(currentTime, slot));
        } catch (Exception exception) {
            log.warn("Invalid support schedule snapshot for session {}: {}", session.getId(), exception.getMessage());
            return false;
        }
    }

    private boolean isWithinSlot(LocalTime currentTime, Document slot) {
        LocalTime start = LocalTime.parse(slot.getString("start"));
        LocalTime end = LocalTime.parse(slot.getString("end"));
        return !currentTime.isBefore(start) && currentTime.isBefore(end);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
