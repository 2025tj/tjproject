package com.tj.tjp.common.exception;

import com.tj.tjp.common.dto.ApiResponse;
import com.tj.tjp.common.dto.ErrorResponse;
import com.tj.tjp.domain.auth.security.exception.RefreshTokenReuseDetectedException;
import com.tj.tjp.dto.error.EmailVerificationErrorResponse;
import com.tj.tjp.dto.error.OAuth2ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RefreshTokenReuseDetectedException.class)
    public ResponseEntity<ApiResponse<Void>> handleReuse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("REAUTH_REQUIRED", "세션 이상 동작이 감지됐습니다. 다시 로그인해주세요."));
    }

    // === OAuth2 관련 예외 (전용 DTO 사용) ===

    @ExceptionHandler(OAuth2LinkRequiredException.class)
    public ResponseEntity<OAuth2ErrorResponse> handleLinkRequired(OAuth2LinkRequiredException ex) {
        log.warn("OAuth2 link required: {} - {}", ex.getEmail(), ex.getProvider());
        OAuth2ErrorResponse response = OAuth2ErrorResponse.linkRequired(ex.getEmail(), ex.getProvider());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(OAuth2SignupRequiredException.class)
    public ResponseEntity<OAuth2ErrorResponse> handleSignupRequired(OAuth2SignupRequiredException ex) {
        log.warn("OAuth2 signup required: {} - {}", ex.getEmail(), ex.getProvider());
        OAuth2ErrorResponse response = OAuth2ErrorResponse.signupRequired(
                ex.getEmail(), ex.getProvider(), ex.getProviderId()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<OAuth2ErrorResponse> handleOAuth2Error(OAuth2AuthenticationException ex) {
        OAuth2Error error = ex.getError();
        log.warn("OAuth2 authentication error: {} - {}", error.getErrorCode(), error.getDescription());

        OAuth2ErrorResponse response = OAuth2ErrorResponse.builder()
                .error(error.getErrorCode())
                .message(error.getDescription())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(OAuth2ProviderNotLinkedException.class)
    public ResponseEntity<OAuth2ErrorResponse> handleProviderNotLinked(OAuth2ProviderNotLinkedException ex) {
        OAuth2Error error = ex.getError();
        log.warn("OAuth2 provider not linked: {}", error.getDescription());

        OAuth2ErrorResponse response = OAuth2ErrorResponse.builder()
                .error(error.getErrorCode())
                .message(error.getDescription())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<EmailVerificationErrorResponse> handleEmailNotVerified(EmailNotVerifiedException ex) {
        log.warn("Email not verified: {}", ex.getEmail());
        EmailVerificationErrorResponse response = EmailVerificationErrorResponse.unverified(
                ex.getEmail(), ex.isCanResend()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // === 일반적인 예외 (ApiResponse 사용) ===

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateUserException(
            DuplicateUserException ex, WebRequest request) {
        log.warn("Duplicate user: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("DUPLICATE_USER", ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("ACCESS_DENIED", "접근 권한이 없습니다."));
    }

    // === Validation 예외 (ErrorResponse 사용) ===

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("VALIDATION_FAILED")
                .message("입력값 검증에 실패했습니다.")
                .errors(validationErrors)
                .path(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // === 기타 예외들 ===

    @ExceptionHandler(FieldErrorException.class)
    public ResponseEntity<Map<String, String>> handleFieldError(FieldErrorException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getField(), ex.getMessage());
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_ARGUMENT", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("ILLEGAL_STATE", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
    }
}
