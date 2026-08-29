package fit.iuh.se.hschat.service.authorization.impl;

import fit.iuh.se.hschat.service.authorization.EpisodeHealthRecordAuthorizationService;
import fit.iuh.se.hshealthrecord.event.HealthRecordAvailableForCareEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HealthRecordCareAuthorizationEventHandler {

    EpisodeHealthRecordAuthorizationService authorizationService;

    @EventListener
    @Transactional
    public void onHealthRecordAvailable(HealthRecordAvailableForCareEvent event) {
        authorizationService.authorizeCreatedRecord(event.getMemberId(), event.getRecordId());
    }
}
