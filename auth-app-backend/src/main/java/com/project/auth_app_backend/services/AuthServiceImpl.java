package com.project.auth_app_backend.services;

import com.project.auth_app_backend.dtos.UserDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto registerUser(UserDto userDto) {
        log.info("Registering new user with email: {}", userDto.getEmail());

        try {
            log.debug("Encoding password for user: {}", userDto.getEmail());
            userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));

            UserDto savedUser = userService.createUser(userDto);

            log.info("User successfully registered with ID: {} and email: {}", 
                    savedUser.getId(), savedUser.getEmail());

            return savedUser;
            
        } catch (Exception e) {
            log.error("Failed to register user {}: {}", userDto.getEmail(), e.getMessage());
            throw e; 
        }
    }
}
