package fit.iuh.se.hsbilling.repository;

import fit.iuh.se.hsbilling.entity.CreditWallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CreditWalletRepository extends JpaRepository<CreditWallet, Long> {
    Optional<CreditWallet> findByMemberId(Long memberId);

    List<CreditWallet> findAllByMemberIdIn(Collection<Long> memberIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from CreditWallet w where w.memberId = :memberId")
    Optional<CreditWallet> findByMemberIdForUpdate(Long memberId);

    @Modifying
    @Query(value = """
        INSERT INTO credit_wallets (id, member_id, balance, reserved, version, created_at, updated_at)
        VALUES (:id, :memberId, 0, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (member_id) DO NOTHING
        """, nativeQuery = true)
    void initialize(Long id, Long memberId);
}
