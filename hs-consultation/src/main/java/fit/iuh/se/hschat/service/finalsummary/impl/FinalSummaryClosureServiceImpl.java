package fit.iuh.se.hschat.service.finalsummary.impl;

import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationFinalSummaryStatus;
import fit.iuh.se.hschat.entity.enums.FinalSummaryClosureStatus;
import fit.iuh.se.hschat.repository.ConsultationFinalSummaryRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.finalsummary.FinalSummaryClosureService;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import fit.iuh.se.hsoperations.dto.command.*;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.service.OperationalEventService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FinalSummaryClosureServiceImpl implements FinalSummaryClosureService {

    static final String DISABLED_DOCTOR_REASON =
            "Assigned Doctor is not active before Final Care Summary finalization";

    ConsultationSessionRepository sessionRepository;
    ConsultationFinalSummaryRepository summaryRepository;
    UserAccountRepository userAccountRepository;
    OperationalEventService operationalEventService;

    @NonFinal
    @Value("${app.consultation.final-summary-due-hours:24}")
    long summaryDueHours;

    @Override
    @Transactional
    public void onSessionCompleted(ConsultationSession session, Instant completedAt) {
        boolean finalized = summaryRepository.findBySessionId(session.getId())
                .filter(summary -> summary.getStatus() == ConsultationFinalSummaryStatus.FINALIZED)
                .isPresent();
        session.setSummaryDueAt(completedAt.plus(Duration.ofHours(summaryDueHours)));
        if (finalized) {
            markFinalized(session);
        } else if (!isActiveDoctor(session.getDoctorId())) {
            markEscalated(session, completedAt);
        } else {
            session.setSummaryClosureStatus(FinalSummaryClosureStatus.SUMMARY_PENDING);
            session.setSummaryEscalatedAt(null);
            session.setSummaryEscalationReason(null);
        }
        sessionRepository.save(session);
        if (!finalized) recordClosure(session,
                session.getSummaryClosureStatus() == FinalSummaryClosureStatus.ESCALATED
                        ? BusinessEventType.SUMMARY_ESCALATED : BusinessEventType.SUMMARY_PENDING);
    }

    @Override
    @Transactional
    public void onSummaryFinalized(ConsultationSession session, Instant finalizedAt) {
        markFinalized(session);
        sessionRepository.save(session);
        operationalEventService.resolveNeedsAction(summaryKey(session, "pending"), "Final summary finalized");
        operationalEventService.resolveNeedsAction(summaryKey(session, "overdue"), "Final summary finalized");
        operationalEventService.resolveNeedsAction(summaryKey(session, "escalated"), "Final summary finalized");
        recordClosure(session, BusinessEventType.SUMMARY_FINALIZED);
    }

    @Override
    @Transactional
    public void refreshOpenClosures(Instant now) {
        List<ConsultationSession> sessions = sessionRepository.findBySummaryClosureStatusIn(List.of(
                FinalSummaryClosureStatus.SUMMARY_PENDING,
                FinalSummaryClosureStatus.SUMMARY_OVERDUE));
        sessions.forEach(session -> {
            if (!isActiveDoctor(session.getDoctorId())) {
                markEscalated(session, now);
                sessionRepository.save(session);
                recordClosure(session, BusinessEventType.SUMMARY_ESCALATED);
            } else if (session.getSummaryClosureStatus() == FinalSummaryClosureStatus.SUMMARY_PENDING
                    && session.getSummaryDueAt() != null
                    && !session.getSummaryDueAt().isAfter(now)) {
                session.setSummaryClosureStatus(FinalSummaryClosureStatus.SUMMARY_OVERDUE);
                sessionRepository.save(session);
                recordClosure(session, BusinessEventType.SUMMARY_OVERDUE);
            }
        });
    }

    private boolean isActiveDoctor(Long doctorId) {
        return userAccountRepository.findById(doctorId)
                .filter(account -> account.getRole() == UserRole.DOCTOR)
                .map(UserAccount::getStatus)
                .filter(AccountStatus.ACTIVE::equals)
                .isPresent();
    }

    private void markFinalized(ConsultationSession session) {
        session.setSummaryClosureStatus(FinalSummaryClosureStatus.SUMMARY_FINALIZED);
        session.setSummaryEscalatedAt(null);
        session.setSummaryEscalationReason(null);
    }

    private void markEscalated(ConsultationSession session, Instant now) {
        session.setSummaryClosureStatus(FinalSummaryClosureStatus.ESCALATED);
        session.setSummaryEscalatedAt(now);
        session.setSummaryEscalationReason(DISABLED_DOCTOR_REASON);
    }

    private void recordClosure(ConsultationSession session, BusinessEventType eventType) {
        String suffix = switch (eventType) {
            case SUMMARY_PENDING -> "pending";
            case SUMMARY_OVERDUE -> "overdue";
            case SUMMARY_ESCALATED -> "escalated";
            default -> "finalized";
        };
        NeedsActionIntent action = eventType == BusinessEventType.SUMMARY_FINALIZED ? null
                : new NeedsActionIntent(switch (eventType) {
                    case SUMMARY_OVERDUE -> NeedsActionType.SUMMARY_OVERDUE;
                    case SUMMARY_ESCALATED -> NeedsActionType.SUMMARY_ESCALATED;
                    default -> NeedsActionType.SUMMARY_PENDING;
                }, eventType == BusinessEventType.SUMMARY_ESCALATED ? NeedsActionPriority.CRITICAL
                        : eventType == BusinessEventType.SUMMARY_OVERDUE ? NeedsActionPriority.HIGH : NeedsActionPriority.NORMAL,
                        "Final summary " + suffix, "The care episode requires Final Care Summary follow-up.",
                        BusinessDomainType.FINAL_SUMMARY, session.getId(), UserRole.CARE_COORDINATOR.name(),
                        summaryKey(session, suffix));
        String eventKey = "summary-closure:" + session.getId() + ":" + suffix;
        operationalEventService.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.FINAL_SUMMARY).domainId(session.getId()).eventType(eventType)
                .actorType(BusinessActorType.SYSTEM).sessionId(session.getId()).memberId(session.getMemberId())
                .doctorId(session.getDoctorId()).newState(session.getSummaryClosureStatus().name())
                .reason(session.getSummaryEscalationReason()).idempotencyKey(eventKey).needsAction(action)
                .notifications(eventType == BusinessEventType.SUMMARY_FINALIZED ? List.of()
                        : List.of(
                        new NotificationIntent(session.getDoctorId(), NotificationType.SUMMARY_ACTION_REQUIRED,
                                "Final summary action required", "A care episode requires Final Care Summary action.",
                                BusinessDomainType.SESSION, session.getId(), eventKey + ":doctor"),
                        NotificationIntent.forRole(UserRole.CARE_COORDINATOR,
                                NotificationType.OPERATIONAL_REVIEW_REQUIRED, "Final summary follow-up",
                                "A care episode requires Final Care Summary follow-up.",
                                BusinessDomainType.SESSION, session.getId(), eventKey + ":coordinators")))
                .build());
    }

    private String summaryKey(ConsultationSession session, String suffix) {
        return "summary-action:" + session.getId() + ":" + suffix;
    }
}
