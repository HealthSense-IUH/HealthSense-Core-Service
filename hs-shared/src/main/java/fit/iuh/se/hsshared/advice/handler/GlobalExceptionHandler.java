package fit.iuh.se.hsshared.advice.handler;

import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.i18n.ErrorMessages;
import fit.iuh.se.hsshared.i18n.RequestLanguage;
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
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Chuyển lỗi thành {@link ApiResponse}. Thông báo theo ngôn ngữ của request: header {@code lang} (vi | en), sau đó
 * {@code Accept-Language}; mặc định tiếng Anh (xem RequestLanguage, ErrorMessages).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        return build(exception.getErrorCode(), exception.localizedMessage(locale()));
    }

    /** Thông báo của từng trường đã được Bean Validation dịch theo locale của request (bộ ValidationMessages). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining("; "));
        return build(ErrorCode.VALIDATION_FAILED, message.isBlank() ? codeMessage(ErrorCode.VALIDATION_FAILED) : message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return build(ErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException exception) {
        return build(ErrorCode.USER_NOT_FOUND, ErrorMessages.detail("detail.resource-not-found", null, locale()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestBody(HttpMessageNotReadableException exception) {
        return build(ErrorCode.INVALID_REQUEST_BODY);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleParameterTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String expectedType = exception.getRequiredType() == null ? "?" : exception.getRequiredType().getSimpleName();
        return build(ErrorCode.INVALID_PARAMETER, ErrorMessages.detail("detail.parameter-type-mismatch",
                new Object[]{exception.getName(), expectedType}, locale()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException exception) {
        return build(ErrorCode.INVALID_ARGUMENT, literalOrCode(exception.getMessage(), ErrorCode.INVALID_ARGUMENT));
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
        return build(ErrorCode.ENTITY_NOT_FOUND, literalOrCode(exception.getMessage(), ErrorCode.ENTITY_NOT_FOUND));
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

    private static Locale locale() {
        return RequestLanguage.current();
    }

    private static String codeMessage(ErrorCode errorCode) {
        return ErrorMessages.code(errorCode, locale());
    }

    /** Câu lỗi của thư viện: giữ nguyên nếu cùng ngôn ngữ với request, không thì dùng thông báo chung của mã lỗi. */
    private static String literalOrCode(String literal, ErrorCode errorCode) {
        Locale locale = locale();
        return literal != null && !literal.isBlank() && ErrorMessages.isInLanguage(literal, locale)
                ? literal : ErrorMessages.code(errorCode, locale);
    }

    private ResponseEntity<ApiResponse<Void>> build(ErrorCode errorCode) {
        return build(errorCode, codeMessage(errorCode));
    }

    private ResponseEntity<ApiResponse<Void>> build(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ApiResponse<>(errorCode.getCode(), message));
    }
}
