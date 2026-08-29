package fit.iuh.se.hsapplication.scheduler;

import fit.iuh.se.hschat.service.session.ConsultationSessionService;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationLifecycleScheduler {

    ConsultationSessionService sessionService;

    @Scheduled(
            fixedDelayString = "${app.consultation.lifecycle-fixed-delay-ms:60000}",
            initialDelayString = "${app.consultation.lifecycle-initial-delay-ms:60000}"
    )
    public void advanceLifecycle() {
        sessionService.activateScheduledSessions(UserRole.SUPER_ADMIN);
        sessionService.expireOverdueSessions(UserRole.SUPER_ADMIN);
    }
}
