package fit.iuh.se.hsoperations.service.impl;

import fit.iuh.se.hsoperations.entity.NeedsActionItem;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.repository.NeedsActionItemRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NeedsActionServiceImplTest {
    @Mock NeedsActionItemRepository repository;

    @Test
    void claimAndResolveAreDurableAndRoleScoped() {
        NeedsActionItem item = NeedsActionItem.builder().id(12L).type(NeedsActionType.SUMMARY_OVERDUE)
                .status(NeedsActionStatus.OPEN).priority(NeedsActionPriority.HIGH).title("Summary overdue")
                .description("Final summary requires action").referenceType(BusinessDomainType.FINAL_SUMMARY)
                .referenceId(50L).assignedRole("CARE_COORDINATOR").idempotencyKey("summary:50:overdue")
                .build();
        when(repository.findLockedById(12L)).thenReturn(Optional.of(item));
        when(repository.save(item)).thenReturn(item);
        NeedsActionServiceImpl service = new NeedsActionServiceImpl(repository);

        var claimed = service.claim(UserRole.CARE_COORDINATOR, 7L, 12L);
        assertEquals(NeedsActionStatus.CLAIMED, claimed.status());
        assertEquals(7L, claimed.claimedBy());

        var resolved = service.resolve(UserRole.CARE_COORDINATOR, 7L, 12L, "Summary finalized");
        assertEquals(NeedsActionStatus.RESOLVED, resolved.status());
        assertEquals("Summary finalized", resolved.resolution());
        assertNotNull(resolved.resolvedAt());
    }

    @Test
    void unrelatedRoleCannotClaimWorkItem() {
        NeedsActionItem item = NeedsActionItem.builder().id(13L).type(NeedsActionType.REFUND_PROVIDER_FAILURE)
                .status(NeedsActionStatus.OPEN).priority(NeedsActionPriority.HIGH).title("Refund failed")
                .description("Reconcile provider refund").referenceType(BusinessDomainType.REFUND)
                .referenceId(60L).assignedRole("ADMIN").idempotencyKey("refund:60:failed").build();
        when(repository.findLockedById(13L)).thenReturn(Optional.of(item));

        assertThrows(AppException.class,
                () -> new NeedsActionServiceImpl(repository).claim(UserRole.CARE_COORDINATOR, 7L, 13L));
    }
}
