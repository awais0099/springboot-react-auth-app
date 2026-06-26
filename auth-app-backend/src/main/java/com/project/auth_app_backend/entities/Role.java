package com.project.auth_app_backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "roles")
public class Role {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(unique = true, nullable = false)
    private String name;
    private String description;
}
