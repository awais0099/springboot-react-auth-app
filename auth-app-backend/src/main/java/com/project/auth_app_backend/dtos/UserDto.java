package com.project.auth_app_backend.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.auth_app_backend.entities.Provider;
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
public class UserDto {
    private UUID id;
    private String email;
    private String name;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String image;
    private boolean enable = true;
    
    // Using @JsonFormat makes these readable in the frontend
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt = Instant.now();
    
    private Instant updatedAt = Instant.now();
    private Provider provider = Provider.LOCAL;
    
    private Set<String> roles = new HashSet<>(); 
}