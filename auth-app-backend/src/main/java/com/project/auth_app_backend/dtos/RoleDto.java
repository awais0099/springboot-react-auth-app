package com.project.auth_app_backend.dtos;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RoleDto {
    private UUID id;
    private String name;
}
