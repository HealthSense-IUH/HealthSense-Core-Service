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
    DATA_INTEGRITY_VIOLATION(3001, "Data violates system constraints", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
