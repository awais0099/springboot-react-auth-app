package com.project.auth_app_backend.controllers;

import com.project.auth_app_backend.dtos.ApiError;
import com.project.auth_app_backend.dtos.UserDto;
import com.project.auth_app_backend.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@Tag(name = "2. User Management", description = "Endpoints for managing core user accounts, administrative provisioning, data lookups, and identity profiles.")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    private final UserService userService;

    @PostMapping
    @Operation(
        summary = "Provision a new user account manually",
        description = "Administrative endpoint to create a pre-verified or custom user record within the system. For self-registration, use the public authentication endpoints."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User record successfully provisioned.", content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Inbound registration data parameters invalid or email collision occurred.", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Requesting context lacks valid Bearer credentials.", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Account credentials lack sufficient privileges (Admin access required).", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        log.info("API Request: Create new user with email: {}", userDto.getEmail());
        UserDto createdUser = userService.createUser(userDto);
        log.info("API Response: User created successfully with ID: {}", createdUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping
    @Operation(
        summary = "Retrieve all registered user accounts",
        description = "Fetches a collection listing of all system accounts. Ideal for administration dashboard interfaces."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User list successfully loaded."),
        @ApiResponse(responseCode = "401", description = "Requesting context lacks valid security credentials.", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Insufficient role permissions to view the user registry.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Iterable<UserDto>> getAllUsers() {
        log.info("API Request: Fetching all users list");
        Iterable<UserDto> users = userService.getAllUsers();
        log.debug("API Response: Successfully retrieved users list.");
        return ResponseEntity.ok(users);
    }

    @GetMapping("/email/{email}")
    @Operation(
        summary = "Look up a user account by email address",
        description = "Queries the database registry for a unique active user account matching the provided email parameter."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Target user account isolated and returned.", content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "401", description = "Requesting context lacks authentication clearance.", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "44", description = "No user found matching the requested email identifier.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<UserDto> getUserByEmail(
            @Parameter(description = "The unique registration email address to search for", example = "developer@example.com") 
            @PathVariable("email") String email
    ) {
        log.info("API Request: Fetching user details for email: {}", email);
        UserDto user = userService.getUserByEmail(email);
        log.debug("API Response: User found for email: {}", email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    @Operation(
        summary = "Update an existing user account profile",
        description = "Applies structural metadata changes to an isolated user record. Note: Changed values should reflect UUID standard constraints."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User changes successfully updated and saved.", content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Provided request body contains invalid data states or types.", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Requesting context lacks authentication clearance.", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "44", description = "Target user record could not be found for the provided identifier.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<UserDto> updateUser(
            @RequestBody UserDto userDto, 
            @Parameter(description = "The target database unique identifier string of the user", example = "fa88c520-2216-43d8-a89c-097a892b9bd6") 
            @PathVariable("userId") String userId
    ) {
        log.info("API Request: Updating user with ID: {}", userId);
        UserDto updatedUser = userService.updateUser(userDto, userId);
        log.info("API Response: User with ID: {} updated successfully", userId);
        return ResponseEntity.ok(updatedUser);
    }
}