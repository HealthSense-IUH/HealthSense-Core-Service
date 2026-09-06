package fit.iuh.se.hschat.service.finalsummary.impl;

import fit.iuh.se.hschat.entity.ConsultationFinalSummary;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.ConsultationFinalSummaryRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.DoctorCareProfileRepository;
import fit.iuh.se.hschat.service.dispatch.event.DoctorDispatchStatusChanged;
import fit.iuh.se.hschat.service.doctor.SupportScheduleValidator;
import fit.iuh.se.hschat.service.finalsummary.QueueFinalSummaryLifecycleService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsoperations.dto.command.NotificationIntent;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QueueFinalSummaryLifecycleServiceImpl implements QueueFinalSummaryLifecycleService {

    static final List<ConsultationStatus> INCOMPATIBLE_SESSION_STATUSES = List.of(
            ConsultationStatus.SCHEDULED, ConsultationStatus.ACTIVE);

    ConsultationSessionRepository sessionRepository;
    DoctorCareProfileRepository profileRepository;
    ConsultationFinalSummaryRepository summaryRepository;
    UserAccountRepository userAccountRepository;
    SupportScheduleValidator scheduleValidator;
    OperationalEventPublisher operationalEventPublisher;
    ApplicationEventPublisher applicationEventPublisher;

    @NonFinal
    @Value("${app.consultation.queue-summary.deadline-minutes:10}")
    long summaryDeadlineMinutes = 10;

    @Override
    @Transactional
    public void onSummaryFinalized(ConsultationSession session, DoctorCareProfile profile,
            UserAccount doctor, ConsultationFinalSummary summary, Instant now) {
        requireQueueCompletedSession(session);
        if (summary.getStatus() != ConsultationFinalSummaryStatus.FINALIZED)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        Instant expectedDueAt = session.getCompletedAt() == null ? null
                : session.getCompletedAt().plus(Duration.ofMinutes(summaryDeadlineMinutes));
        if (expectedDueAt != null && !expectedDueAt.equals(session.getSummaryDueAt()))
            session.setSummaryDueAt(expectedDueAt);

        session.setSummaryClosureStatus(FinalSummaryClosureStatus.SUMMARY_FINALIZED);
        session.setSummaryEscalatedAt(null);
        session.setSummaryEscalationReason(null);
        sessionRepository.save(session);
        recordSummaryFinalized(session, summary, now);

        if (session.getDoctorReleasedAt() != null) return;
        boolean timely = summary.getFinalizedAt() != null && session.getSummaryDueAt() != null
                && summary.getFinalizedAt().isBefore(session.getSummaryDueAt());
        release(session, profile, doctor, now,
                timely ? DoctorReleaseReason.SUMMARY_FINALIZED : DoctorReleaseReason.SUMMARY_TIMEOUT,
                timely);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findDueSessionIds(Instant now, int limit) {
        return sessionRepository.findQueueSummaryReleaseCandidateIds(
                ConsultationFlowType.QUEUE_DISPATCH_V1,
                ConsultationStatus.COMPLETED,
                FinalSummaryClosureStatus.SUMMARY_PENDING,
                FinalSummaryClosureStatus.SUMMARY_FINALIZED,
                now,
                now.minus(Duration.ofMinutes(summaryDeadlineMinutes)),
                PageRequest.of(0, limit));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDeadline(Long sessionId, Instant now) {
        ConsultationSession session = sessionRepository.findByIdForUpdate(sessionId).orElse(null);
        if (session == null || session.getFlowType() != ConsultationFlowType.QUEUE_DISPATCH_V1
                || session.getStatus() != ConsultationStatus.COMPLETED
                || session.getDoctorReleasedAt() != null) return;

        Instant expectedDueAt = session.getCompletedAt() == null ? null
                : session.getCompletedAt().plus(Duration.ofMinutes(summaryDeadlineMinutes));
        if (expectedDueAt != null && !expectedDueAt.equals(session.getSummaryDueAt())) {
            session.setSummaryDueAt(expectedDueAt);
            sessionRepository.save(session);
        }

        UserAccount doctor = userAccountRepository.findByIdForUpdate(session.getDoctorId()).orElse(null);
        DoctorCareProfile profile = profileRepository.findByDoctorIdForUpdate(session.getDoctorId()).orElse(null);
        ConsultationFinalSummary summary = summaryRepository.findBySessionIdForUpdate(sessionId).orElse(null);

        if (summary != null && summary.getStatus() == ConsultationFinalSummaryStatus.FINALIZED) {
            session.setSummaryClosureStatus(FinalSummaryClosureStatus.SUMMARY_FINALIZED);
            Instant finalizedAt = summary.getFinalizedAt();
            boolean timely = finalizedAt != null && session.getSummaryDueAt() != null
                    && finalizedAt.isBefore(session.getSummaryDueAt());
            release(session, profile, doctor, now,
                    timely ? DoctorReleaseReason.SUMMARY_FINALIZED : DoctorReleaseReason.SUMMARY_TIMEOUT,
                    timely);
            return;
        }

        if (session.getSummaryClosureStatus() != FinalSummaryClosureStatus.SUMMARY_PENDING
                || session.getSummaryDueAt() == null || now.isBefore(session.getSummaryDueAt())) return;
        release(session, profile, doctor, now, DoctorReleaseReason.SUMMARY_TIMEOUT, false);
    }

    private void release(ConsultationSession session, DoctorCareProfile profile, UserAccount doctor,
            Instant releasedAt, DoctorReleaseReason reason, boolean timely) {
        DoctorDispatchStatus previous = profile == null ? null : profile.getDispatchStatus();
        boolean ownsDoctor = profile != null
                && previous == DoctorDispatchStatus.BUSY
                && session.getId().equals(profile.getBusySessionId());
        DoctorDispatchStatus target = reason == DoctorReleaseReason.SUMMARY_TIMEOUT
                || !timely || !eligibleForAvailability(profile, doctor)
                || Boolean.TRUE.equals(profile.getStopAfterCurrentSession())
                ? DoctorDispatchStatus.UNAVAILABLE : DoctorDispatchStatus.AVAILABLE;

        if (ownsDoctor) {
            profile.setDispatchStatus(target);
            profile.setBusySessionId(null);
            profile.setDispatchStatusChangedAt(releasedAt);
            profileRepository.save(profile);
        }

        session.setDoctorReleasedAt(releasedAt);
        session.setDoctorReleaseReason(reason);
        sessionRepository.save(session);
        recordRelease(session, profile, previous, target, releasedAt, reason, ownsDoctor);

        if (ownsDoctor && previous != target)
            applicationEventPublisher.publishEvent(new DoctorDispatchStatusChanged(
                    session.getDoctorId(), previous, target, releasedAt));
    }

    private boolean eligibleForAvailability(DoctorCareProfile profile, UserAccount doctor) {
        return profile != null && doctor != null
                && doctor.getRole() == UserRole.DOCTOR
                && doctor.getStatus() == AccountStatus.ACTIVE
                && Boolean.TRUE.equals(profile.getAcceptsOneOnOneCare())
                && profile.getSpecialty() != null
                && profile.getMaxActiveConsultations() != null
                && profile.getMaxActiveConsultations() > 0
                && scheduleValidator.isValid(profile.getAvailabilityJson(), profile.getTimezone(), true)
                && !sessionRepository.existsByDoctorIdAndStatusIn(
                profile.getDoctorId(), INCOMPATIBLE_SESSION_STATUSES);
    }

    private void requireQueueCompletedSession(ConsultationSession session) {
        if (session.getFlowType() != ConsultationFlowType.QUEUE_DISPATCH_V1
                || session.getStatus() != ConsultationStatus.COMPLETED)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);
    }

    private void recordSummaryFinalized(
            ConsultationSession session, ConsultationFinalSummary summary, Instant now) {
        operationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.FINAL_SUMMARY).domainId(summary.getId())
                .eventType(BusinessEventType.SUMMARY_FINALIZED).actorType(BusinessActorType.SYSTEM)
                .sessionId(session.getId()).summaryId(summary.getId()).memberId(session.getMemberId())
                .doctorId(session.getDoctorId()).newState(FinalSummaryClosureStatus.SUMMARY_FINALIZED.name())
                .occurredAt(now).idempotencyKey("queue-session:" + session.getId() + ":summary-finalized")
                .notifications(List.of()).build());
    }

    private void recordRelease(ConsultationSession session, DoctorCareProfile profile,
            DoctorDispatchStatus previous, DoctorDispatchStatus target, Instant releasedAt,
            DoctorReleaseReason reason, boolean ownsDoctor) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("completedAt", String.valueOf(session.getCompletedAt()));
        metadata.put("summaryDueAt", String.valueOf(session.getSummaryDueAt()));
        metadata.put("doctorReleasedAt", releasedAt.toString());
        metadata.put("doctorReleaseReason", reason.name());
        metadata.put("stopAfterCurrentSession", Boolean.toString(
                profile != null && Boolean.TRUE.equals(profile.getStopAfterCurrentSession())));
        metadata.put("busySessionOwnershipMatched", Boolean.toString(ownsDoctor));

        String key = "queue-session:" + session.getId() + ":doctor-release:" + reason;
        BusinessEventType eventType = reason == DoctorReleaseReason.SUMMARY_TIMEOUT
                ? BusinessEventType.DOCTOR_SUMMARY_RELEASE_TIMEOUT
                : target == DoctorDispatchStatus.AVAILABLE
                ? BusinessEventType.DOCTOR_AVAILABLE : BusinessEventType.DOCTOR_UNAVAILABLE;
        List<NotificationIntent> notifications = reason == DoctorReleaseReason.SUMMARY_TIMEOUT
                ? List.of(new NotificationIntent(session.getDoctorId(),
                        NotificationType.DOCTOR_SUMMARY_DEADLINE_EXPIRED,
                        "Final summary deadline expired",
                        "Consultation intake was disabled because the Final Summary was not completed on time. You can still finalize it from session history.",
                        BusinessDomainType.SESSION, session.getId(), key + ":doctor"))
                : List.of();
        operationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.ACCOUNT).domainId(session.getDoctorId())
                .eventType(eventType).actorType(BusinessActorType.SYSTEM)
                .sessionId(session.getId()).memberId(session.getMemberId()).doctorId(session.getDoctorId())
                .previousState(previous == null ? null : previous.name())
                .newState(ownsDoctor ? target.name() : previous == null ? null : previous.name())
                .reason(reason.name()).metadata(metadata).occurredAt(releasedAt)
                .idempotencyKey(key).notifications(notifications).build());

        if (ownsDoctor && reason == DoctorReleaseReason.SUMMARY_TIMEOUT)
            operationalEventPublisher.record(OperationalEventCommand.builder()
                    .domainType(BusinessDomainType.ACCOUNT).domainId(session.getDoctorId())
                    .eventType(BusinessEventType.DOCTOR_UNAVAILABLE).actorType(BusinessActorType.SYSTEM)
                    .sessionId(session.getId()).memberId(session.getMemberId()).doctorId(session.getDoctorId())
                    .previousState(previous.name()).newState(target.name()).reason(reason.name())
                    .metadata(metadata).occurredAt(releasedAt).idempotencyKey(key + ":status")
                    .notifications(List.of()).build());
    }
}
