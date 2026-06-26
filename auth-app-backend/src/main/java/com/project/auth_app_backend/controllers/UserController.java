package com.project.auth_app_backend.controllers;

import com.project.auth_app_backend.dtos.UserDto;
import com.project.auth_app_backend.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    // create user api
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        log.info("API Request: Create new user with email: {}", userDto.getEmail());
        
        UserDto createdUser = userService.createUser(userDto);
        
        log.info("API Response: User created successfully with ID: {}", createdUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // get all user api
    @GetMapping
    public ResponseEntity<Iterable<UserDto>> getAllUsers() {
        log.info("API Request: Fetching all users list");
        
        Iterable<UserDto> users = userService.getAllUsers();
        
        log.debug("API Response: Successfully retrieved users list.");
        return ResponseEntity.ok(users);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable("email") String email) {
        log.info("API Request: Fetching user details for email: {}", email);
        
        UserDto user = userService.getUserByEmail(email);
        
        log.debug("API Response: User found for email: {}", email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto, @PathVariable("userId") String userId) {
        log.info("API Request: Updating user with ID: {}", userId);
        
        UserDto updatedUser = userService.updateUser(userDto, userId);
        
        log.info("API Response: User with ID: {} updated successfully", userId);
        return ResponseEntity.ok(updatedUser);
    }
}