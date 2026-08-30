package fit.iuh.se.hsoperations.service;

import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.entity.BusinessAuditEvent;

public interface OperationalEventService {
    BusinessAuditEvent record(OperationalEventCommand command);
    void resolveNeedsAction(String idempotencyKey, String resolution);
}
