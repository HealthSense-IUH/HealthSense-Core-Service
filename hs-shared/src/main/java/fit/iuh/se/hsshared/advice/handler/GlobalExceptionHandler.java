package fit.iuh.se.hsshared.advice.handler;

import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return build(errorCode, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        if (message.isBlank()) {
            message = errorCode.getMessage();
        }
        return build(errorCode, message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return build(ErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestBody(HttpMessageNotReadableException exception) {
        return build(ErrorCode.INVALID_REQUEST_BODY);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleParameterTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String expectedType = exception.getRequiredType() == null
                ? "valid type"
                : exception.getRequiredType().getSimpleName();
        String message = exception.getName() + " must be " + expectedType;
        return build(ErrorCode.INVALID_PARAMETER, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException exception) {
        String message = exception.getMessage() == null || exception.getMessage().isBlank()
                ? ErrorCode.INVALID_ARGUMENT.getMessage()
                : exception.getMessage();
        return build(ErrorCode.INVALID_ARGUMENT, message);
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(Exception exception) {
        return build(ErrorCode.INVALID_CREDENTIALS);
    }

    @ExceptionHandler({
            JwtException.class,
            MissingRequestCookieException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(Exception exception) {
        return build(ErrorCode.INVALID_TOKEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException exception) {
        return build(ErrorCode.ACCESS_DENIED);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabledException(DisabledException exception) {
        return build(ErrorCode.ACCOUNT_DISABLED);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException exception) {
        String message = exception.getMessage() == null || exception.getMessage().isBlank()
                ? ErrorCode.ENTITY_NOT_FOUND.getMessage()
                : exception.getMessage();
        return build(ErrorCode.ENTITY_NOT_FOUND, message);
    }

    @ExceptionHandler({
            SQLIntegrityConstraintViolationException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(Exception exception) {
        log.warn("Data integrity violation: {}", exception.getMessage());
        return build(ErrorCode.DATA_INTEGRITY_VIOLATION);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUncategorizedException(Exception exception) {
        log.error("Uncategorized exception", exception);
        return build(ErrorCode.UNCATEGORIZED);
    }

    private ResponseEntity<ApiResponse<Void>> build(ErrorCode errorCode) {
        return build(errorCode, errorCode.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> build(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ApiResponse<>(errorCode.getCode(), message));
    }
}
