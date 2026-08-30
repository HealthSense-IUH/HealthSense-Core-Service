package fit.iuh.se.hsshared.advice.entity.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Common
    BAD_REQUEST(400, "Bad request", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, "Unauthorized", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(403, "Access denied", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND(404, "User not found", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(405, "Method not allowed", HttpStatus.METHOD_NOT_ALLOWED),
    TOO_MANY_REQUESTS(429, "Too many requests", HttpStatus.TOO_MANY_REQUESTS),
    UNCATEGORIZED(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    // Validation
    VALIDATION_FAILED(1200, "Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_PARAMETER(1201, "Invalid parameter", HttpStatus.BAD_REQUEST),
    INVALID_ARGUMENT(1202, "Invalid argument", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_BODY(1203, "Invalid request body", HttpStatus.BAD_REQUEST),

    // Security & authentication
    INVALID_CREDENTIALS(1001, "Invalid email or password", HttpStatus.UNAUTHORIZED),
    EMAIL_ALREADY_EXISTS(1002, "Email already exists", HttpStatus.CONFLICT),
    ACCOUNT_DISABLED(1003, "Account is disabled", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_INVALID(1004, "Refresh token is invalid", HttpStatus.UNAUTHORIZED),
    SESSION_NOT_FOUND(1005, "Session was not found", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REUSED(1006, "Refresh token was already used", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1007, "Token is invalid or expired", HttpStatus.UNAUTHORIZED),

    // Data & persistence
    ENTITY_NOT_FOUND(3000, "Entity was not found", HttpStatus.NOT_FOUND),
    DATA_INTEGRITY_VIOLATION(3001, "Data violates system constraints", HttpStatus.CONFLICT),
    HEALTH_RECORD_NOT_FOUND(3002, "Health record not found", HttpStatus.NOT_FOUND),

    // Consultation
    CONSULTATION_NOT_FOUND(4000, "Consultation session not found", HttpStatus.NOT_FOUND),
    CONSULTATION_REQUEST_NOT_FOUND(4001, "Consultation request not found", HttpStatus.NOT_FOUND),
    CONSULTATION_ACCESS_DENIED(4002, "You do not have access to this consultation", HttpStatus.FORBIDDEN),
    CONSULTATION_NOT_ACTIVE(4003, "Consultation session is not active", HttpStatus.CONFLICT),
    MEMBER_ALREADY_HAS_ACTIVE_CONSULTATION(4004, "Member already has an active consultation", HttpStatus.CONFLICT),
    MEMBER_ALREADY_HAS_PENDING_CONSULTATION_REQUEST(4005, "Member already has a pending consultation request", HttpStatus.CONFLICT),
    DOCTOR_CAPACITY_EXCEEDED(4006, "Doctor has reached the maximum active consultations", HttpStatus.CONFLICT),
    DOCTOR_NOT_FOUND(4007, "Doctor not found", HttpStatus.NOT_FOUND),
    MEMBER_NOT_FOUND(4008, "Member not found", HttpStatus.NOT_FOUND),
    INVALID_CONSULTATION_STATUS(4009, "Invalid consultation status", HttpStatus.CONFLICT),
    CONSULTATION_MESSAGE_NOT_FOUND(4010, "Consultation message not found", HttpStatus.NOT_FOUND),
    CONSULTATION_PARTICIPANT_NOT_FOUND(4011, "Consultation participant not found", HttpStatus.NOT_FOUND),
    CARE_SERVICE_PACKAGE_NOT_FOUND(4012, "Care service package not found", HttpStatus.NOT_FOUND),
    DOCTOR_CARE_PROFILE_NOT_FOUND(4013, "Doctor care profile not found", HttpStatus.NOT_FOUND),
    DOCTOR_NOT_ELIGIBLE_FOR_CONSULTATION(4014, "Doctor is not eligible for consultation", HttpStatus.CONFLICT),
    INVALID_DOCTOR_SUPPORT_SCHEDULE(4015, "Doctor support schedule is invalid", HttpStatus.BAD_REQUEST),
    CARE_SERVICE_PACKAGE_CODE_ALREADY_EXISTS(4016, "Care service package code already exists", HttpStatus.CONFLICT),
    INVALID_CARE_SERVICE_PACKAGE_STATUS(4017, "Invalid care service package status transition", HttpStatus.CONFLICT),
    CONSULTATION_PAYMENT_NOT_FOUND(4018, "Consultation payment not found", HttpStatus.NOT_FOUND),
    INVALID_CONSULTATION_PAYMENT_STATUS(4019, "Invalid consultation payment status", HttpStatus.CONFLICT),
    PAYMENT_PROVIDER_NOT_CONFIGURED(4020, "Payment provider is not configured", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_PROVIDER_ERROR(4021, "Payment provider error", HttpStatus.BAD_GATEWAY),
    INVALID_PAYMENT_WEBHOOK(4022, "Invalid payment webhook", HttpStatus.BAD_REQUEST),
    CONSULTATION_REFUND_NOT_FOUND(4023, "Consultation refund not found", HttpStatus.NOT_FOUND),
    INVALID_REFUND_STATUS(4024, "Invalid refund status", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
