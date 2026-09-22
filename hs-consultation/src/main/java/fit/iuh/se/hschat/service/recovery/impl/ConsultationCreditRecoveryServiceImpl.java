package fit.iuh.se.hschat.service.recovery.impl;

import fit.iuh.se.hsbilling.entity.enums.CreditReservationStatus;
import fit.iuh.se.hsbilling.service.ConsultationCreditService;
import fit.iuh.se.hschat.dto.response.CreditRecoveryResponse;
import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
import fit.iuh.se.hschat.repository.ConsultationRequestRepository;
import fit.iuh.se.hschat.service.recovery.ConsultationCreditRecoveryService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConsultationCreditRecoveryServiceImpl implements ConsultationCreditRecoveryService {
    private static final List<ConsultationRequestStatus> TERMINAL = List.of(
            ConsultationRequestStatus.CANCELLED, ConsultationRequestStatus.TIMED_OUT,
            ConsultationRequestStatus.EXPIRED, ConsultationRequestStatus.REJECTED);
    private final ConsultationRequestRepository requests;
    private final ConsultationCreditService credits;

    @Override @Transactional
    public CreditRecoveryResponse recover(Long actorId, UserRole role, int limit) {
        if (actorId == null || (role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN))
            throw new AppException(ErrorCode.ACCESS_DENIED);
        if (limit < 1 || limit > 100) throw new AppException(ErrorCode.INVALID_PARAMETER, "limit must be between 1 and 100");
        int examined=0,released=0,already=0; List<String> issues=new ArrayList<>();
        for(Long id:requests.findTerminalPaidRequestIds(TERMINAL,PageRequest.of(0,limit))) {
            examined++;
            var request=requests.findByIdForUpdate(id).orElse(null);
            if(request==null||!TERMINAL.contains(request.getStatus())||request.getConsultationSessionId()!=null) continue;
            try {
                var before=credits.inspectReservation(request.getMemberId(),id);
                if(before.status()==CreditReservationStatus.HELD) {
                    credits.release(request.getMemberId(),id,"recovery:terminal-request:"+request.getStatus()); released++;
                } else if(before.status()==CreditReservationStatus.RELEASED) already++;
                else issues.add("request="+id+":terminal-but-captured");
            } catch(AppException ex) {
                if(ex.getErrorCode()==ErrorCode.CREDIT_RESERVATION_NOT_FOUND)
                    issues.add("request="+id+":missing-reservation");
                else throw ex;
            }
        }
        int remaining=Math.max(0,limit-examined);
        for(Long id:requests.findFulfilledPaidRequestIds(PageRequest.of(0,remaining))) {
            examined++;
            var request=requests.findById(id).orElse(null); if(request==null) continue;
            try {
                var reservation=credits.inspectReservation(request.getMemberId(),id);
                if(reservation.status()!=CreditReservationStatus.CAPTURED
                        || !Objects.equals(reservation.sessionId(),request.getConsultationSessionId()))
                    issues.add("request="+id+":fulfilled-reservation-mismatch");
            } catch(AppException ex) {
                if(ex.getErrorCode()==ErrorCode.CREDIT_RESERVATION_NOT_FOUND)
                    issues.add("request="+id+":fulfilled-missing-reservation");
                else throw ex;
            }
        }
        return new CreditRecoveryResponse(examined,released,already,List.copyOf(issues));
    }
}
