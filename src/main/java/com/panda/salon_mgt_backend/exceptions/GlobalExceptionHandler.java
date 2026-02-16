package com.panda.salon_mgt_backend.exceptions;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleSalonExists(AlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(DeactivateException.class)
    public ResponseEntity<ErrorResponse> handleStaffInactive(DeactivateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(InactiveException.class)
    public ResponseEntity<ErrorResponse> handleStaffInactive(InactiveException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(CanNotException.class)
    public ResponseEntity<ErrorResponse> handleStaffInactive(CanNotException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            CredentialsExpiredException.class,
            JwtException.class,
            AuthenticationException.class
    })
    public ResponseEntity<ApiError> handleAuthExceptions(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        if (ex instanceof DisabledException) {
            status = HttpStatus.FORBIDDEN;
        } else if (ex instanceof LockedException) {
            status = HttpStatus.LOCKED;
        }
        ApiError body = ApiError.of(status, "Authentication error", safeMessage(ex), request.getRequestURI());
        return ResponseEntity.status(status)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("Pragma", "no-cache")
                .body(body);
    }

    @ExceptionHandler(RefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleRefreshTokenException(RefreshTokenException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private String safeMessage(Exception ex) {
        // Avoid leaking sensitive details in prod
        String msg = ex.getMessage();
        return (msg == null || msg.isBlank()) ? "Invalid or expired credentials" : msg;
    }

    public record ApiError(
            OffsetDateTime timestamp,
            int status,
            String error,
            String message,
            String path
    ) {
        public static ApiError of(HttpStatus status, String error, String message, String path) {
            return new ApiError(OffsetDateTime.now(ZoneOffset.UTC), status.value(), error, message, path);
        }
    }

}
