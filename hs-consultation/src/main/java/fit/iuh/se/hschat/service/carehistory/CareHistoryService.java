package fit.iuh.se.hschat.service.carehistory;

import fit.iuh.se.hschat.dto.response.CareContinuitySummaryResponse;
import fit.iuh.se.hschat.dto.response.CareHistoryEpisodeResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CareHistoryService {

    PageResponse<CareHistoryEpisodeResponse> getMemberHistory(Long memberId, Pageable pageable);

    CareHistoryEpisodeResponse getMemberEpisode(Long memberId, Long sessionId);

    List<CareContinuitySummaryResponse> getContinuitySummaries(Long doctorId, Long currentSessionId);
}
