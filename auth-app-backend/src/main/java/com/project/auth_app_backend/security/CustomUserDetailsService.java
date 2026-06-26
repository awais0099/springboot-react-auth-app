package com.project.auth_app_backend.security;

import com.project.auth_app_backend.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Security: Attempting to load user by email for authentication: {}", username);

        return userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.warn("Security: Authentication failed - User not found with email: {}", username);
                    return new BadCredentialsException("Invalid Email or Password !!");
                });
    }
}