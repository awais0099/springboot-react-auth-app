package com.project.auth_app_backend.dtos;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
        HttpStatus status,
        int statusCode,
        String message
) {
}
