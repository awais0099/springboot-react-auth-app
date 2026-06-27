package com.project.auth_app_backend.services;

import com.project.auth_app_backend.dtos.UserDto;
import com.project.auth_app_backend.entities.Role;
import com.project.auth_app_backend.entities.User;
import com.project.auth_app_backend.exceptions.RegistrationException;
import com.project.auth_app_backend.repositories.RoleRepository;
import com.project.auth_app_backend.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // Injecting your role database access vector
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper mapper;

    @Override
    @Transactional
    public UserDto registerUser(UserDto userDto) {
        log.info("Auth Core: Processing registration sequence for email: {}", userDto.getEmail());

        // 1. Proactively intercept email duplication
        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.warn("Auth Blocked: Registration aborted. Email {} already active in database.", userDto.getEmail());
            throw new RegistrationException("An account is already registered with this email address.");
        }

        // 2. Fetch the persistent Role entity from the database
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> {
                    log.error("Database Configuration Error: Target role 'ROLE_USER' not found in database!");
                    return new RuntimeException("System configuration error: Default user role does not exist.");
                });

        try {
            // 3. Map input data straight onto a secure entity footprint
            User userEntity = mapper.map(userDto, User.class);
            
            log.debug("Auth Core: Applying cryptographic shielding to user password.");
            userEntity.setPassword(passwordEncoder.encode(userDto.getPassword()));
            
            // Set operational fields
            userEntity.setEnable(true); 
            
            // Assign the real persistent database object to the user's roles collection
            userEntity.setRoles(Set.of(defaultRole)); 

            User savedUser = userRepository.save(userEntity);
            log.info("Auth Success: Account provisioned successfully. ID: {} | Email: {}", savedUser.getId(), savedUser.getEmail());

            return mapper.map(savedUser, UserDto.class);
            
        } catch (Exception e) {
            log.error("Auth System Fault: Failed to write user transaction for {}: {}", userDto.getEmail(), e.getMessage());
            throw new RuntimeException("Account provisioning failed due to an internal system error.");
        }
    }
}