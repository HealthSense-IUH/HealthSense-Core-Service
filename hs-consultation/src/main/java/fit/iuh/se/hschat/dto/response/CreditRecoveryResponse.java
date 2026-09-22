package fit.iuh.se.hschat.dto.response;

import java.util.List;

public record CreditRecoveryResponse(int examined, int released, int alreadyTerminal,
        List<String> integrityIssues) {}
