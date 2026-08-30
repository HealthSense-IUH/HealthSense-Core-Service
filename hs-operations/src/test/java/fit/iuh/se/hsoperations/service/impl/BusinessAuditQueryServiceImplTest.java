package fit.iuh.se.hsoperations.service.impl;

import fit.iuh.se.hsoperations.entity.BusinessAuditEvent;
import fit.iuh.se.hsoperations.entity.enums.BusinessDomainType;
import fit.iuh.se.hsoperations.repository.BusinessAuditEventRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessAuditQueryServiceImplTest {
    @Mock BusinessAuditEventRepository repository;

    @Test
    void memberAndDoctorCannotAccessRawBusinessAudit() {
        BusinessAuditQueryServiceImpl service = new BusinessAuditQueryServiceImpl(repository);

        assertThrows(AppException.class, () -> service.find(UserRole.MEMBER, null, null, null, PageRequest.of(0, 20)));
        assertThrows(AppException.class, () -> service.find(UserRole.DOCTOR, null, null, null, PageRequest.of(0, 20)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void coordinatorScopeExcludesHealthRecordAndAccountAudit() {
        when(repository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(Page.empty());
        BusinessAuditQueryServiceImpl service = new BusinessAuditQueryServiceImpl(repository);

        service.find(UserRole.CARE_COORDINATOR, null, null, null, PageRequest.of(0, 20));

        ArgumentCaptor<Specification<BusinessAuditEvent>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(repository).findAll(captor.capture(), any(PageRequest.class));
        Root<BusinessAuditEvent> root = org.mockito.Mockito.mock(Root.class);
        CriteriaQuery<?> query = org.mockito.Mockito.mock(CriteriaQuery.class);
        CriteriaBuilder builder = org.mockito.Mockito.mock(CriteriaBuilder.class);
        Path domainPath = org.mockito.Mockito.mock(Path.class);
        when(root.get("domainType")).thenReturn(domainPath);

        captor.getValue().toPredicate(root, query, builder);

        verify(domainPath).in(BusinessDomainType.REQUEST, BusinessDomainType.RESERVATION,
                BusinessDomainType.AGREEMENT, BusinessDomainType.PAYMENT, BusinessDomainType.SESSION,
                BusinessDomainType.RENEWAL, BusinessDomainType.REFUND, BusinessDomainType.FINAL_SUMMARY);
    }
}
