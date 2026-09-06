package fit.iuh.se.hschat.service.message.impl;

import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationParticipantRole;
import fit.iuh.se.hschat.entity.enums.ConsultationFlowType;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.service.message.SupportHoursPolicy;
import fit.iuh.se.hschat.service.continuation.ContinuationStore;
import fit.iuh.se.hsshared.advice.entity.AppException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SnapshotSupportHoursPolicy implements SupportHoursPolicy {

    private final ContinuationStore continuationStore;

    @NonFinal
    @Value("${app.consultation.continuation.grace-minutes:5}")
    long graceMinutes = 5;

    @NonFinal
    Clock clock = Clock.systemUTC();

    @Override
    public boolean canSendNow(ConsultationSession session, ConsultationParticipantRole role) {
        if (session.getFlowType() == ConsultationFlowType.QUEUE_DISPATCH_V1)
            return queueChatWritable(session);
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

    private boolean queueChatWritable(ConsultationSession session) {
        if (session.getStatus() != ConsultationStatus.ACTIVE || session.getEndsAt() == null) return false;
        Instant now = Instant.now(clock);
        if (now.isBefore(session.getEndsAt())) return true;
        Instant graceExpiresAt = session.getEndsAt().plus(Duration.ofMinutes(graceMinutes));
        if (!now.isBefore(graceExpiresAt)) return false;
        try {
            return continuationStore.find(session.getId(), session.getContinuationRound())
                    .filter(state -> state.promptedAt().equals(session.getEndsAt()))
                    .filter(state -> state.graceExpiresAt().equals(graceExpiresAt))
                    .isPresent();
        } catch (AppException exception) {
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
