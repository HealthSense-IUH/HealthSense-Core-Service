package fit.iuh.se.hsoperations.service.impl;

import fit.iuh.se.hsoperations.repository.BusinessAuditEventRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    void adminCanAccessAllBusinessAuditDomains() {
        when(repository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(Page.empty());
        BusinessAuditQueryServiceImpl service = new BusinessAuditQueryServiceImpl(repository);

        service.find(UserRole.ADMIN, null, null, null, PageRequest.of(0, 20));

        verify(repository).findAll(any(Specification.class), any(PageRequest.class));
    }
}
