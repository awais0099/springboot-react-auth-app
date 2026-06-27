package com.project.auth_app_backend.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response package delivered upon successful validation of identity credentials, containing state tokens and sanitized user profile data")
public record TokenResponse(
        @Schema(description = "Short-lived cryptographic JSON Web Token enabling immediate access to secured resources", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Long-lived storage token utilized to safely provision new access keys without re-authenticating credentials", example = "8f3b9d0a-4c2e-4b6a-9f1d-7e5c3b1a2d4e")
        String refreshToken,

        @Schema(description = "Access token lifespan window calculated in total seconds", example = "3600")
        long expiresIn,

        @Schema(description = "Prefix schema used to authenticate access token keys within authorization header packets", example = "Bearer")
        String tokenType,

        @Schema(description = "Sanitized account record mapping profile of the authenticated user")
        UserDto user
) {
        
    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn, UserDto user) {
        return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer", user);
    }

    public TokenResponse(String accessToken, String refreshToken, long expiresIn, UserDto user) {
        this(accessToken, refreshToken, expiresIn, "Bearer", user);
    }
}