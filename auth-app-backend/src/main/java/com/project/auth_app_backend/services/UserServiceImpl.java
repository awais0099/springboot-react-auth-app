package com.project.auth_app_backend.services;

import com.project.auth_app_backend.dtos.UserDto;
import com.project.auth_app_backend.entities.Provider;
import com.project.auth_app_backend.entities.User;
import com.project.auth_app_backend.exceptions.ResourceNotFoundException;
import com.project.auth_app_backend.helpers.UserHelper;
import com.project.auth_app_backend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.debug("UserService: Attempting to create user with email: {}", userDto.getEmail());
        
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            log.warn("UserService: Create failed - Email is null or blank");
            throw new IllegalArgumentException("Email is required");
        }
        
        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.warn("UserService: Create failed - Email {} already exists", userDto.getEmail());
            throw new IllegalArgumentException("User with given email already exists");
        }

        User user = modelMapper.map(userDto, User.class);
        user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);
        
        log.trace("UserService: Mapping UserDto to Entity for email: {}", userDto.getEmail());

        User savedUser = userRepository.save(user);
        log.info("UserService: User created successfully with ID: {}", savedUser.getId());
        
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        log.debug("UserService: Fetching user by email: {}", email);
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("UserService: User fetch failed - No user found with email: {}", email);
                    return new ResourceNotFoundException("User not found with given email");
                });
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {
        log.info("UserService: Attempting to update user with ID: {}", userId);
        UUID uid = UserHelper.parseUUID(userId);
        
        User existingUser = userRepository
                .findById(uid)
                .orElseThrow(() -> {
                    log.error("UserService: Update failed - User ID {} not found", userId);
                    return new ResourceNotFoundException("User not found with the given id: " + userId);
                });

        // Track what is being changed at a DEBUG level
        if (userDto.getName() != null) {
            log.debug("UserService: Updating name for user {}: {} -> {}", userId, existingUser.getName(), userDto.getName());
            existingUser.setName(userDto.getName());
        }
        
        if (userDto.getImage() != null) existingUser.setImage(userDto.getImage());
        if (userDto.getProvider() != null) existingUser.setProvider(userDto.getProvider());

        if (userDto.getPassword() != null) {
            log.trace("UserService: Password update requested for user {}", userId);
            existingUser.setPassword(userDto.getPassword());
        }

        existingUser.setEnable(userDto.isEnable());
        existingUser.setUpdatedAt(Instant.now());
        
        User updatedUser = userRepository.save(existingUser);
        log.info("UserService: User with ID {} updated successfully", userId);
        
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {
        log.warn("UserService: Request to DELETE user with ID: {}", userId);
        UUID uId = UserHelper.parseUUID(userId);
        
        User user = userRepository.findById(uId)
                .orElseThrow(() -> {
                    log.error("UserService: Delete failed - User ID {} not found", userId);
                    return new ResourceNotFoundException("User not found with given id");
                });
                
        userRepository.delete(user);
        log.info("UserService: User with ID {} has been permanently deleted", userId);
    }

    @Override
    public UserDto getUserById(String userId) {
        log.debug("UserService: Fetching user by ID: {}", userId);
        User user = userRepository.findById(UserHelper.parseUUID(userId))
                .orElseThrow(() -> {
                    log.warn("UserService: Fetch failed - User ID {} not found", userId);
                    return new ResourceNotFoundException("User not found with given id");
                });
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public Iterable<UserDto> getAllUsers() {
        log.debug("UserService: Fetching all users from database");
        List<User> users = userRepository.findAll();
        log.trace("UserService: Retrieved {} users", users.size());
        
        return users.stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }
}