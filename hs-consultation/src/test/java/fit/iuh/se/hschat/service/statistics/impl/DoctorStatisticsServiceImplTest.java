package fit.iuh.se.hschat.service.statistics.impl;

import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientHealthRecordResponse;
import fit.iuh.se.hschat.entity.enums.EpisodeHealthRecordAuthorizationSource;
import fit.iuh.se.hschat.repository.statistics.DoctorConsultationStatisticsSummary;
import fit.iuh.se.hschat.repository.statistics.DoctorPatientStatisticsSummary;
import fit.iuh.se.hschat.repository.statistics.DoctorStatisticsQueryRepository;
import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsshared.advice.entity.AppException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorStatisticsServiceImplTest {

    @Mock
    DoctorStatisticsQueryRepository repository;

    @Mock
    OperationalEventPublisher operationalEvents;

    @InjectMocks
    DoctorStatisticsServiceImpl service;

    private final Instant from = Instant.parse("2026-09-01T00:00:00Z");
    private final Instant to = Instant.parse("2026-10-01T00:00:00Z");

    @Test
    void returnsConsultationKpisAndPercentageRates() {
        when(repository.summarizeConsultations(44L, from, to))
                .thenReturn(new DoctorConsultationStatisticsSummary(30, 10, 2, 1, 6, 1));

        var response = service.getConsultationStatistics(44L, from, to);

        assertEquals(30, response.summary().totalConsultations());
        assertEquals(10, response.summary().consultationsInPeriod());
        assertEquals(new BigDecimal("60.00"), response.summary().completionRate());
        assertEquals(new BigDecimal("10.00"), response.summary().cancellationRate());
    }

    @Test
    void returnsDistinctPatientKpisAndAverageForThePeriod() {
        when(repository.summarizePatients(44L, from, to))
                .thenReturn(new DoctorPatientStatisticsSummary(20, 4, 1, 3, 7));

        var response = service.getPatientStatistics(44L, from, to);

        assertEquals(20, response.summary().totalPatients());
        assertEquals(4, response.summary().patientsInPeriod());
        assertEquals(1, response.summary().newPatientsInPeriod());
        assertEquals(3, response.summary().returningPatientsInPeriod());
        assertEquals(new BigDecimal("1.75"), response.summary().averageConsultationsPerPatientInPeriod());
    }

    @Test
    void rejectsInvalidRangeBeforeQuerying() {
        assertThrows(AppException.class, () -> service.getConsultationStatistics(44L, to, from));
        verifyNoInteractions(repository, operationalEvents);
    }

    @Test
    void rejectsPatientOutsideDoctorRelationship() {
        when(repository.hasPatientRelationship(44L, 99L)).thenReturn(false);

        assertThrows(AppException.class,
                () -> service.getPatientMedicalHistory(44L, 99L, PageRequest.of(0, 20)));

        verify(repository).hasPatientRelationship(44L, 99L);
    }

    @Test
    void listsOnlyAuthorizedHealthRecordsAndAuditsReturnedRecords() {
        var pageable = PageRequest.of(0, 20);
        var record = new DoctorPatientHealthRecordResponse(
                501L,
                301L,
                "ecg.csv",
                1200L,
                RecordStatus.COMPLETED,
                PredictionLabel.NORMAL,
                0.95,
                Instant.parse("2026-09-20T08:00:00Z"),
                EpisodeHealthRecordAuthorizationSource.INITIAL_SHARED,
                Instant.parse("2026-09-20T08:01:00Z"));
        when(repository.hasPatientRelationship(44L, 99L)).thenReturn(true);
        when(repository.findAuthorizedHealthRecords(44L, 99L, pageable))
                .thenReturn(new PageImpl<>(List.of(record), pageable, 1));

        var response = service.getPatientHealthRecords(44L, 99L, pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals(501L, response.getContent().getFirst().recordId());
        verify(operationalEvents).record(any());
    }

    @Test
    void doesNotAuditWhenRelationshipIsMissing() {
        when(repository.hasPatientRelationship(44L, 99L)).thenReturn(false);

        assertThrows(AppException.class,
                () -> service.getPatientHealthRecords(44L, 99L, PageRequest.of(0, 20)));

        verify(operationalEvents, never()).record(any());
    }
}
