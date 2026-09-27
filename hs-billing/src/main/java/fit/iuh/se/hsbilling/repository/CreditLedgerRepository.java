package fit.iuh.se.hsbilling.repository;

import fit.iuh.se.hsbilling.entity.CreditLedgerEntry;
import fit.iuh.se.hsbilling.entity.enums.CreditOperation;
import fit.iuh.se.hsbilling.entity.enums.CreditSourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;

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
