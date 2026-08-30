package fit.iuh.se.hsoperations.service.impl;

import fit.iuh.se.hsoperations.entity.BusinessAuditEvent;
import fit.iuh.se.hsoperations.service.BusinessAuditQueryService;
import fit.iuh.se.hsoperations.service.OperationalEventService;
import jakarta.persistence.Column;
import org.hibernate.annotations.Immutable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BusinessAuditImmutabilityTest {
    @Test
    void auditEntityAndNormalServiceContractsAreAppendOnly() {
        assertTrue(BusinessAuditEvent.class.isAnnotationPresent(Immutable.class));
        for (Field field : BusinessAuditEvent.class.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) assertFalse(column.updatable(), field.getName() + " must be immutable");
        }

        Set<String> writeMethods = Set.of("update", "edit", "delete", "remove");
        assertTrue(Arrays.stream(BusinessAuditQueryService.class.getMethods())
                .noneMatch(method -> writeMethods.contains(method.getName())));
        assertTrue(Arrays.stream(OperationalEventService.class.getMethods())
                .noneMatch(method -> writeMethods.contains(method.getName())));
    }
}
