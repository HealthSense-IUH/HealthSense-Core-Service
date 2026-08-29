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
    }

    @Override
    @Transactional
    public void onSummaryFinalized(ConsultationSession session, Instant finalizedAt) {
        markFinalized(session);
        sessionRepository.save(session);
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
            } else if (session.getSummaryClosureStatus() == FinalSummaryClosureStatus.SUMMARY_PENDING
                    && session.getSummaryDueAt() != null
                    && !session.getSummaryDueAt().isAfter(now)) {
                session.setSummaryClosureStatus(FinalSummaryClosureStatus.SUMMARY_OVERDUE);
                sessionRepository.save(session);
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
}
