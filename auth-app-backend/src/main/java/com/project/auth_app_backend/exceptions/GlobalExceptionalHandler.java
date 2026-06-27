package com.project.auth_app_backend.exceptions;

import com.project.auth_app_backend.dtos.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionalHandler {

    /**
     * Catches authentication and verification drops. 
     * Enforces the 401 Unauthorized contract expected by the Axios Interceptor.
     */
    @ExceptionHandler({
            UsernameNotFoundException.class,
            BadCredentialsException.class,
            CredentialsExpiredException.class
    })
    public ResponseEntity<ApiError> handleUnauthorizedException(Exception e, HttpServletRequest request) {
        log.warn("Auth Blocked: [{}] {} | Reason: {}", request.getMethod(), request.getRequestURI(), e.getMessage());

        var apiError = new ApiError(
                HttpStatus.UNAUTHORIZED.value(), 
                HttpStatus.UNAUTHORIZED.getReasonPhrase(), 
                "Invalid username, password, or expired session criteria.", 
                request.getRequestURI(),
                null
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    /**
     * Catches recognized credentials that lack structural permission (like a Disabled account context).
     * Enforces the 403 Forbidden boundary layer.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabledException(DisabledException e, HttpServletRequest request) {
        log.warn("Account Suspended: [{}] {} | Reason: {}", request.getMethod(), request.getRequestURI(), e.getMessage());

        var apiError = new ApiError(
                HttpStatus.FORBIDDEN.value(), 
                HttpStatus.FORBIDDEN.getReasonPhrase(), 
                "This account structure has been administratively disabled.", 
                request.getRequestURI(),
                null
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    /**
     * Standardized to use the uniform ApiError contract for clean UI schema parsing.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(ResourceNotFoundException exception, HttpServletRequest request) {
        log.warn("Target Discovered Empty at {}: {}", request.getRequestURI(), exception.getMessage());
        
        var apiError = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }
    
    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ApiError> handleRegistrationException(RegistrationException exception, HttpServletRequest request) {
        log.warn("Target Discovered Empty at {}: {}", request.getRequestURI(), exception.getMessage());
        
        var apiError = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException exception, HttpServletRequest request) {
        log.warn("Bad Payload Input for {}: {}", request.getRequestURI(), exception.getMessage());
        
        var apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    /**
     * "Catch-All" fallback loop preserving server integrity traces.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleInternalServerError(Exception e, HttpServletRequest request) {
        log.error("CRITICAL SYSTEM ERROR at [{}]: ", request.getRequestURI(), e);
        
        var apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), 
                "An unexpected error occurred. Please contact support.", 
                request.getRequestURI(),
                null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}