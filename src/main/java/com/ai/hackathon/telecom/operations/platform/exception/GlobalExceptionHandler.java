package com.ai.hackathon.telecom.operations.platform.exception;

import com.ai.hackathon.telecom.operations.platform.audit.AuditService;
import com.ai.hackathon.telecom.operations.platform.auth.EmailAlreadyExistsException;
import com.ai.hackathon.telecom.operations.platform.call.CallNotFoundException;
import com.ai.hackathon.telecom.operations.platform.dtos.ApiResponse;
import com.twilio.exception.ApiException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashSet;
import java.util.Set;

import static com.ai.hackathon.telecom.operations.platform.exception.BusinessErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AuditService auditService;
    private final HttpServletRequest httpServletRequest;

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleException(LockedException exp) {
        return ResponseEntity
                .status(ACCOUNT_LOCKED.getHttpStatus())
                .body(ApiResponse.error(
                        ACCOUNT_LOCKED.getHttpStatus().value(),
                        ACCOUNT_LOCKED.getDescription(),
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_LOCKED.getCode())
                                .businessErrorDescription(ACCOUNT_LOCKED.getDescription())
                                .error(exp.getMessage())
                                .build()
                ));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleException(DisabledException exp) {
        return ResponseEntity
                .status(ACCOUNT_DISABLED.getHttpStatus())
                .body(ApiResponse.error(
                        ACCOUNT_DISABLED.getHttpStatus().value(),
                        ACCOUNT_DISABLED.getDescription(),
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_DISABLED.getCode())
                                .businessErrorDescription(ACCOUNT_DISABLED.getDescription())
                                .error(exp.getMessage())
                                .build()
                ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleException(BadCredentialsException exp) {
        return ResponseEntity
                .status(BAD_CREDENTIALS.getHttpStatus())
                .body(ApiResponse.error(
                        BAD_CREDENTIALS.getHttpStatus().value(),
                        BAD_CREDENTIALS.getDescription(),
                        ExceptionResponse.builder()
                                .businessErrorCode(BAD_CREDENTIALS.getCode())
                                .businessErrorDescription(BAD_CREDENTIALS.getDescription())
                                .error("Username or Password is incorrect")
                                .build()
                ));
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleException(MessagingException exp) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        INTERNAL_SERVER_ERROR.value(),
                        exp.getMessage(),
                        ExceptionResponse.builder()
                                .error(exp.getMessage())
                                .build()
                ));
    }

    @ExceptionHandler(ActivationTokenException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleException(ActivationTokenException exp) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ApiResponse.error(
                        BAD_REQUEST.value(),
                        exp.getMessage(),
                        ExceptionResponse.builder()
                                .error(exp.getMessage())
                                .build()
                ));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleException(EmailAlreadyExistsException exp) {
        return ResponseEntity
                .status(EMAIL_ALREADY_EXISTS.getHttpStatus())
                .body(ApiResponse.error(
                        EMAIL_ALREADY_EXISTS.getHttpStatus().value(),
                        EMAIL_ALREADY_EXISTS.getDescription(),
                        ExceptionResponse.builder()
                                .businessErrorCode(EMAIL_ALREADY_EXISTS.getCode())
                                .businessErrorDescription(EMAIL_ALREADY_EXISTS.getDescription())
                                .error(exp.getMessage())
                                .build()
                ));
    }

    @ExceptionHandler(OperationNotPermittedException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleException(OperationNotPermittedException exp) {
        return ResponseEntity
                .status(FORBIDDEN)
                .body(ApiResponse.error(
                        FORBIDDEN.value(),
                        exp.getMessage(),
                        ExceptionResponse.builder()
                                .error(exp.getMessage())
                                .build()
                ));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleException(UsernameNotFoundException exp) {
        return ResponseEntity
                .status(USER_NOT_FOUND.getHttpStatus())
                .body(ApiResponse.error(
                        USER_NOT_FOUND.getHttpStatus().value(),
                        USER_NOT_FOUND.getDescription(),
                        ExceptionResponse.builder()
                                .businessErrorCode(USER_NOT_FOUND.getCode())
                                .businessErrorDescription(USER_NOT_FOUND.getDescription())
                                .error(exp.getMessage())
                                .build()
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleException(AccessDeniedException exp) {
        return ResponseEntity
                .status(ACCESS_DENIED.getHttpStatus())
                .body(ApiResponse.error(
                        ACCESS_DENIED.getHttpStatus().value(),
                        ACCESS_DENIED.getDescription(),
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCESS_DENIED.getCode())
                                .businessErrorDescription(ACCESS_DENIED.getDescription())
                                .error(exp.getMessage())
                                .build()
                ));
    }

    @ExceptionHandler(CallNotFoundException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleException(CallNotFoundException exp) {
        return ResponseEntity
                .status(CALL_NOT_FOUND.getHttpStatus())
                .body(ApiResponse.error(
                        CALL_NOT_FOUND.getHttpStatus().value(),
                        CALL_NOT_FOUND.getDescription(),
                        ExceptionResponse.builder()
                                .businessErrorCode(CALL_NOT_FOUND.getCode())
                                .businessErrorDescription(CALL_NOT_FOUND.getDescription())
                                .error(exp.getMessage())
                                .build()
                ));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleException(ApiException exp) {
        log.error("Twilio API error: {}", exp.getMessage(), exp);
        return ResponseEntity
                .status(CALL_INITIATION_FAILED.getHttpStatus())
                .body(ApiResponse.error(
                        CALL_INITIATION_FAILED.getHttpStatus().value(),
                        CALL_INITIATION_FAILED.getDescription(),
                        ExceptionResponse.builder()
                                .businessErrorCode(CALL_INITIATION_FAILED.getCode())
                                .businessErrorDescription(CALL_INITIATION_FAILED.getDescription())
                                .error(exp.getMessage())
                                .build()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exp) {
        Set<String> errors = new HashSet<>();
        exp.getBindingResult().getAllErrors()
                .forEach(error -> {
                    var errorMessage = error.getDefaultMessage();
                    errors.add(errorMessage);
                });
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ApiResponse.error(
                        BAD_REQUEST.value(),
                        "Validation failed",
                        ExceptionResponse.builder()
                                .validationErrors(errors)
                                .build()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleException(Exception exp) {
        log.error("Unhandled exception", exp);
        auditService.logSystemError(httpServletRequest, exp);
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        INTERNAL_SERVER_ERROR.value(),
                        "Internal Server error",
                        ExceptionResponse.builder()
                                .businessErrorDescription("Internal Server error")
                                .error(exp.getMessage())
                                .build()
                ));
    }
}
