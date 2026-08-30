package fit.iuh.se.hsapplication.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminBusinessAuditControllerContractTest {
    @Test
    void auditApiExposesReadOnlyOperations() {
        Method[] methods = AdminBusinessAuditController.class.getDeclaredMethods();

        assertTrue(Arrays.stream(methods).allMatch(method -> method.isAnnotationPresent(GetMapping.class)));
        assertFalse(Arrays.stream(methods).anyMatch(method -> method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PatchMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class)));
    }
}
