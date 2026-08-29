package fit.iuh.se.hschat.service.authorization.impl;

import fit.iuh.se.hschat.service.authorization.EpisodeHealthRecordAuthorizationService;
import fit.iuh.se.hshealthrecord.event.HealthRecordAvailableForCareEvent;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class HealthRecordCareAuthorizationEventHandlerTest {

    @Test
    void confirmedCareRecordEventCreatesEpisodeAuthorization() {
        EpisodeHealthRecordAuthorizationService service = mock(EpisodeHealthRecordAuthorizationService.class);
        HealthRecordCareAuthorizationEventHandler handler = new HealthRecordCareAuthorizationEventHandler(service);

        handler.onHealthRecordAvailable(HealthRecordAvailableForCareEvent.builder()
                .recordId(200L)
                .memberId(1L)
                .availableAt(java.time.Instant.now())
                .build());

        verify(service).authorizeCreatedRecord(1L, 200L);
    }
}
