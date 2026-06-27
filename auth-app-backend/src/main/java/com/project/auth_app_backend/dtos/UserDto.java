package com.project.auth_app_backend.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.auth_app_backend.entities.Provider;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "Data Transfer Object representing the uniform profile shape of a platform user record")
public class UserDto {

    @Schema(
        description = "Unique Identifier (System assigned)", 
        example = "4e6b1892-0b1e-437b-9c76-2184bf3f49df", 
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private UUID id;

    @Schema(
        description = "Primary distinct email address used for credential mapping and access verification", 
        example = "developer@example.com", 
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @Schema(
        description = "Full formal or display name of the user profile container", 
        example = "John Doe", 
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(
        description = "Plain text password string. Handled strictly as write-only and instantly cryptographically shielded during ingestion processing.", 
        example = "StrongP@ss123!", 
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    @Schema(
        description = "Absolute URI web link endpoint or reference pointer to user profile display picture asset", 
        example = "https://images.example.com/profiles/john-doe.jpg"
    )
    private String image;

    @Schema(
        description = "System operational status marker defining active login accessibility", 
        example = "true"
    )
    private boolean enable = true;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(
        description = "Timestamp instance indicating baseline record initialization moment (ISO 8601 string format)", 
        example = "2026-06-27T10:58:20Z", 
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Instant createdAt = Instant.now();

    @Schema(
        description = "Timestamp instance indicating the trailing data mutation update event (ISO 8601 string format)", 
        example = "2026-06-27T10:58:20Z", 
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Instant updatedAt = Instant.now();

    @Schema(
        description = "Identity registration origin provider context distinguishing authenticating source pathways", 
        example = "LOCAL"
    )
    private Provider provider = Provider.LOCAL;

    @Schema(
        description = "Collection of standard access string permissions attached directly onto the user's active authority group mapping context", 
        example = "[\"ROLE_USER\"]"
    )
    private Set<String> roles = new HashSet<>(); 
}