package com.project.auth_app_backend.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Inbound authentication payload containing client identity credentials required to generate a session token pair")
public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(
            description = "Registered user account email address acting as the unique identity key identifier", 
            example = "developer@example.com", 
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        String email,

        @NotBlank(message = "Password is required")
        @Schema(
            description = "Secret raw character string used to verify user identity claims authenticity", 
            example = "StrongP@ss123!", 
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        String password
) {}