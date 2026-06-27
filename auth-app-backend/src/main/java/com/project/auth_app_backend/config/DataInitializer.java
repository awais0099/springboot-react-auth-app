package com.project.auth_app_backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.project.auth_app_backend.entities.Role;
import com.project.auth_app_backend.repositories.RoleRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        if (!roleRepository.existsByName("ROLE_USER")) {
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            userRole.setDescription("Standard authenticated subscriber authorization.");
            roleRepository.save(userRole);
        }

        if (!roleRepository.existsByName("ROLE_ADMIN")) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setDescription("Elevated administrative controls and telemetry systems access.");
            roleRepository.save(adminRole);
        }
    }
}
