package com.project.auth_app_backend.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Inbound structural body payload holding renewal parameters required to refresh an expired access token string")
public record RefreshTokenRequest(
        @Schema(
            description = "Valid, unexpired refresh token string previously provisioned during login", 
            example = "8f3b9d0a-4c2e-4b6a-9f1d-7e5c3b1a2d4e", 
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        String refreshToken
) {
}