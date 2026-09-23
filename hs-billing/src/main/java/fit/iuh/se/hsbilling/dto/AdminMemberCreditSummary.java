package fit.iuh.se.hsbilling.dto;

import fit.iuh.se.hsuser.entity.enums.AccountStatus;

import java.time.Instant;

public record AdminMemberCreditSummary(
        String memberId,
        String displayName,
        String email,
        String phone,
        AccountStatus accountStatus,
        String avatarUrl,
        boolean walletInitialized,
        long balance,
        long reserved,
        long available,
        Instant walletUpdatedAt) {
}
