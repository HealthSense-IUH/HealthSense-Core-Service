package fit.iuh.se.hsoperations.service.impl;

import fit.iuh.se.hsoperations.event.NotificationProjectionRequested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class NotificationProjectionListenerTest {
    @Test
    void projectionFailureCannotEscapeAfterCommitListener() {
        NotificationProjector projector = mock(NotificationProjector.class);
        doThrow(new RuntimeException("in-app delivery unavailable")).when(projector).project(9L);

        assertDoesNotThrow(() -> new NotificationProjectionListener(projector)
                .afterCommit(new NotificationProjectionRequested(9L)));
    }
}
