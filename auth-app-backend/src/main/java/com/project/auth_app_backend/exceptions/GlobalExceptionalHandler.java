package com.project.auth_app_backend.exceptions;

import com.project.auth_app_backend.dtos.ApiError;
import com.project.auth_app_backend.dtos.ErrorResponse;
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

    @ExceptionHandler({
            UsernameNotFoundException.class,
            BadCredentialsException.class,
            CredentialsExpiredException.class,
            DisabledException.class
    })
    public ResponseEntity<ApiError> handleAuthException(Exception e, HttpServletRequest request) {
        log.warn("Authentication failed for [{}] {} | Reason: {}", 
                request.getMethod(), 
                request.getRequestURI(), 
                e.getMessage());

        var apiError = ApiError.of(
                HttpStatus.BAD_REQUEST.value(), 
                "Bad Request", 
                e.getMessage(), 
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception, HttpServletRequest request) {
        log.warn("Resource not found at {}: {}", request.getRequestURI(), exception.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, 404, exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception, HttpServletRequest request) {
        log.warn("Invalid argument provided to {}: {}", request.getRequestURI(), exception.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, 400, exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * "Catch-All" handler for unexpected 500 errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleInternalServerError(Exception e, HttpServletRequest request) {
        log.error("CRITICAL SYSTEM ERROR at [{}]: ", request.getRequestURI(), e);
        
        var apiError = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                "Internal Server Error", 
                "An unexpected error occurred. Please contact support.", 
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}