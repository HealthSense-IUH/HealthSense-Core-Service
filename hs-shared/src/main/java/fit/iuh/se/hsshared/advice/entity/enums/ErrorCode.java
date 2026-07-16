package fit.iuh.se.hsshared.advice.entity.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    UNCATEGORIZED(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST(400, "Bad request", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED(401, "Validation failed", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED(403, "Access denied", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND(404, "User not found", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS(1001, "Invalid email or password", HttpStatus.UNAUTHORIZED),
    EMAIL_ALREADY_EXISTS(1002, "Email already exists", HttpStatus.CONFLICT),
    ACCOUNT_DISABLED(1003, "Account is disabled", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_INVALID(1004, "Refresh token is invalid", HttpStatus.UNAUTHORIZED),
    SESSION_NOT_FOUND(1005, "Session was not found", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REUSED(1006, "Refresh token was already used", HttpStatus.UNAUTHORIZED);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

}
