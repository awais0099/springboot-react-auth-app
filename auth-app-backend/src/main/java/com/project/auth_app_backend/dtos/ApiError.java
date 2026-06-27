package com.project.auth_app_backend.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Schema(description = "Standardized error wrapper payload returned across all application layers during a non-2xx exceptional event")
public record ApiError(
        @Schema(description = "HTTP response status status code integer", example = "401")
        int status,
        
        @Schema(description = "Standard HTTP phrasing description matching the failure status code code status category", example = "Unauthorized")
        String error,
        
        @Schema(description = "Human-readable text details isolating the direct cause of the exception", example = "The provided access token has expired.")
        String message,
        
        @Schema(description = "Active target context web request path where the failure sequence triggered", example = "/api/v1/dashboard/metrics")
        String path,
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        @Schema(description = "Precise global server timestamp instance noting when the exception broke (ISO-8601 string sequence standard format)", example = "2026-06-27T11:08:57.123Z", accessMode = Schema.AccessMode.READ_ONLY)
        OffsetDateTime timestamp
) {
    /**
     * Compact Constructor: Automatically runs every single time an ApiError is instantiated.
     */
    public ApiError {
        // Automatically inject the current UTC time right before the record freezes
        timestamp = OffsetDateTime.now(ZoneOffset.UTC);
    }
}