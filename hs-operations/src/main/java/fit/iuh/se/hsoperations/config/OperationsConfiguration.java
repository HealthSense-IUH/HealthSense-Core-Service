package fit.iuh.se.hsoperations.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.se.hsoperations.repository.*;
import fit.iuh.se.hsoperations.service.OperationalEventService;
import fit.iuh.se.hsoperations.service.impl.OperationalEventServiceImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OperationsConfiguration {
    @Bean
    ObjectMapper operationsObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    OperationalEventService operationalEventService(
            BusinessAuditEventRepository auditRepository,
            NeedsActionItemRepository needsActionRepository,
            NotificationProjectionTaskRepository projectionRepository,
            ObjectMapper objectMapper,
            ApplicationEventPublisher eventPublisher) {
        return new OperationalEventServiceImpl(auditRepository, needsActionRepository, projectionRepository,
                objectMapper, eventPublisher);
    }
}
