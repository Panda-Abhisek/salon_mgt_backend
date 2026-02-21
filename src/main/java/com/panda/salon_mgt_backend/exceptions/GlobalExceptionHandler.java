package com.panda.salon_mgt_backend.exceptions;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ================= DOMAIN EXCEPTIONS ================= */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex, req);
    }

    @ExceptionHandler(PlanUpgradeRequiredException.class)
    public ResponseEntity<ApiError> handlePlanUpgrade(
            PlanUpgradeRequiredException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.PAYMENT_REQUIRED; // 402

        ApiError body = ApiError.of(
                status,
                "UPGRADE_REQUIRED",
                "This feature requires " + ex.getRequiredPlan() + " plan",
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler({
            AlreadyExistsException.class,
            DeactivateException.class,
            InactiveException.class,
            CanNotException.class
    })
    public ResponseEntity<ApiError> handleConflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex, req);
    }

    /* ================= VALIDATION ================= */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return build(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /* ================= AUTH ================= */

    @ExceptionHandler({
            BadCredentialsException.class,
            CredentialsExpiredException.class,
            JwtException.class,
            AuthenticationException.class
    })
    public ResponseEntity<ApiError> handleAuth(Exception ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        if (ex instanceof DisabledException) status = HttpStatus.FORBIDDEN;
        if (ex instanceof LockedException) status = HttpStatus.LOCKED;

        return build(status, safeMessage(ex), req);
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ApiError> handleRefresh(RefreshTokenException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ex, req);
    }

    /* ================= FALLBACK ================= */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", req);
    }

    /* ================= HELPERS ================= */

    private ResponseEntity<ApiError> build(HttpStatus status, Exception ex, HttpServletRequest req) {
        return build(status, ex.getMessage(), req);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req) {
        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                req.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    private String safeMessage(Exception ex) {
        String msg = ex.getMessage();
        return (msg == null || msg.isBlank())
                ? "Invalid or expired credentials"
                : msg;
    }
}
