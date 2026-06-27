package com.project.auth_app_backend.dtos;

import lombok.*;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "Data Transfer Object representing granular access permissions and grouping roles assigned to user records")
public class RoleDto {

    @Schema(
        description = "Unique Identifier mapping key (System assigned)", 
        example = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d", 
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private UUID id;

    @Schema(
        description = "Unique formal security identity flag used for authorization checks across code boundary lines", 
        example = "ROLE_USER", 
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;
}
