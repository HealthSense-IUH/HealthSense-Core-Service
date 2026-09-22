package fit.iuh.se.hsbilling.repository;

import fit.iuh.se.hsbilling.entity.CreditLedgerEntry;
import fit.iuh.se.hsbilling.entity.enums.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.time.Instant;
import org.springframework.data.jpa.repository.Query;

// Append-only repository: no update/delete methods exposed to callers.
public interface CreditLedgerRepository extends Repository<CreditLedgerEntry, Long>, JpaSpecificationExecutor<CreditLedgerEntry> {
    CreditLedgerEntry save(CreditLedgerEntry entry);
    Optional<CreditLedgerEntry> findByIdempotencyKey(String key);
    Optional<CreditLedgerEntry> findByOperationAndSourceTypeAndSourceId(
            CreditOperation operation, CreditSourceType sourceType, Long sourceId);
    Page<CreditLedgerEntry> findByWalletId(Long walletId, Pageable pageable);
    @Query("select coalesce(sum(e.deltaBalance),0) from CreditLedgerEntry e where e.walletId=:walletId")
    long balanceTotal(Long walletId);
    @Query("select coalesce(sum(e.deltaReserved),0) from CreditLedgerEntry e where e.walletId=:walletId")
    long reservedTotal(Long walletId);
}
