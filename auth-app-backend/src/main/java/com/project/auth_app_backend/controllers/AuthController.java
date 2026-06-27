package com.project.auth_app_backend.controllers;

import com.project.auth_app_backend.dtos.ApiError;
import com.project.auth_app_backend.dtos.LoginRequest;
import com.project.auth_app_backend.dtos.RefreshTokenRequest;
import com.project.auth_app_backend.dtos.TokenResponse;
import com.project.auth_app_backend.dtos.UserDto;
import com.project.auth_app_backend.entities.RefreshToken;
import com.project.auth_app_backend.entities.User;
import com.project.auth_app_backend.repositories.RefreshTokenRepository;
import com.project.auth_app_backend.repositories.UserRepository;
import com.project.auth_app_backend.security.JwtService;
import com.project.auth_app_backend.services.AuthService;
import com.project.auth_app_backend.services.CookieService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@Tag(name = "1. Identity & Access Management", description = "Endpoints handling registration, session creation, token rotation, and secure logout sequences.")
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final ModelMapper mapper;

    @PostMapping("/register")
    @Operation(
        summary = "Register a new platform user account",
        description = "Validates inbound user attributes, enforces email uniqueness constraints, cryptographically hashes the password, and provisions a default subscriber role."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created successfully.", content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid payload parameters or email already registered.", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Internal system database persistence fault.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        log.info("Auth Entry: Registration attempt for email: {}", userDto.getEmail());
        UserDto savedUserDto = authService.registerUser(userDto);
        log.info("Auth Success: User registered with ID: {}", savedUserDto.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUserDto);
    }

    @PostMapping("/login")
    @Operation(
        summary = "Authenticate user credentials and issue session tokens",
        description = "Verifies password credentials against the database, validates account status flags, saves an active parent Refresh Token record to the database, and returns a token payload while dropping an HttpOnly cookie."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful. Session tokens generated.", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "401", description = "Bad credentials or user account is disabled.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        log.info("Auth Entry: Login attempt for user: {}", loginRequest.email());
        
        authenticate(loginRequest);

        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> {
                    log.error("Auth Failure: User {} not found after successful authentication!", loginRequest.email());
                    return new BadCredentialsException("Invalid Username or Password");
                });

        if (!user.isEnable()) {
            log.warn("Auth Blocked: Attempted login for disabled account: {}", user.getEmail());
            throw new DisabledException("User is disabled");
        }

        String jti = UUID.randomUUID().toString();
        var refreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshTokenOb);
        log.debug("Auth Success: RefreshToken record created in DB with JTI: {}", jti);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenOb.getJti());

        cookieService.attachRefreshCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());
        log.info("Auth Success: User {} logged in. Tokens issued.", user.getEmail());

        TokenResponse tokenResponse = TokenResponse.of(
                accessToken,
                refreshToken,
                jwtService.getAccessTtlSeconds(),
                mapper.map(user, UserDto.class)
        );

        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refresh-token")
    @Operation(
        summary = "Rotate an expired access token using a refresh token",
        description = "Extracts the refresh token from either the HTTP body payload or an HttpOnly cookie fallback. Performs reuse tracking checks, revokes old token trees, and provisions a fresh set of session identifiers."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tokens rotated successfully.", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "401", description = "Refresh token missing, invalid, or expired.", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "SECURITY WARNING: Refresh token reuse detected.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.debug("Auth Entry: Refresh token rotation requested.");

        Optional<String> refreshTokenOptional = readRefreshTokenFromRequest(refreshTokenRequest, request);

        if (refreshTokenOptional.isEmpty()) {
            log.warn("Auth Failure: Refresh request made without token.");
            throw new BadCredentialsException("Refresh token is missing");
        }

        String refreshToken = refreshTokenOptional.get();

        if(!jwtService.isRefreshToken(refreshToken)){
            log.warn("Auth Failure: Token provided for rotation is not a Refresh Token.");
            throw new BadCredentialsException("Invalid Refresh Token Type");
        }

        String jti = jwtService.getJti(refreshToken);
        UUID userId = jwtService.getUserId(refreshToken);
        
        RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> {
                    log.warn("Auth Failure: Refresh token JTI {} not found in database.", jti);
                    return new BadCredentialsException("Refresh token not recognized");
                });

        if (storedRefreshToken.isRevoked()) {
            log.error("SECURITY ALERT: Refresh token reuse detected! JTI: {}. This token was already rotated.", jti);
            throw new RuntimeException("Security Alert: Refresh token reuse detected.");
        }

        if (storedRefreshToken.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Auth Failure: Stored Refresh token {} has expired.", jti);
            throw new RuntimeException("Session expired, please login again.");
        }

        storedRefreshToken.setRevoked(true);
        String newJti = UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);
        refreshTokenRepository.save(storedRefreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found during refresh"));

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user, newJti);

        RefreshToken newToken = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();
        
        refreshTokenRepository.save(newToken);
        log.info("Auth Success: Token rotation completed for user {}. Old JTI: {} -> New JTI: {}", user.getEmail(), jti, newJti);
        
        cookieService.attachRefreshCookie(response, newRefreshToken, (int) jwtService.getRefreshTtlSeconds());

        return ResponseEntity.ok(new TokenResponse(newAccessToken, newRefreshToken, jwtService.getAccessTtlSeconds(), "refresh", mapper.map(user, UserDto.class)));
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Terminate the active user session",
        description = "Extracts current cookie credentials to clear server-side states. Marks current parent refresh tokens as permanently revoked, drops clean expired replacement cookies, and clears contexts.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "24", description = "Session terminated successfully. Context completely cleared."),
        @ApiResponse(responseCode = "401", description = "Access credentials invalid or missing context verification.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Auth Entry: Logout requested.");

        readRefreshTokenFromRequest(null, request).ifPresent(token -> {
            try {
                if (jwtService.isRefreshToken(token)) {
                    String jti = jwtService.getJti(token);
                    refreshTokenRepository.findByJti(jti).ifPresent(rt -> {
                        rt.setRevoked(true);
                        refreshTokenRepository.save(rt);
                        log.debug("Auth Success: Refresh token JTI {} revoked in database.", jti);
                    });
                }
            } catch (JwtException e) {
                log.warn("Auth Warning: Error processing refresh token during logout: {}", e.getMessage());
            }
        });

        cookieService.clearRefreshCookie(response);
        SecurityContextHolder.clearContext();
        
        log.info("Auth Success: User logged out successfully.");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
        log.trace("Utility: Attempting to extract Refresh Token from request...");
        
        if (request.getCookies() != null) {
            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> !v.isBlank())
                    .findFirst();

            if (fromCookie.isPresent()) {
                log.trace("Utility: Refresh Token found in cookies.");
                return fromCookie;
            }
        }

        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            log.trace("Utility: Refresh Token found in request body.");
            return Optional.of(body.refreshToken());
        }

        return Optional.empty();
    }

    private Authentication authenticate(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        } catch (Exception e) {
            log.warn("Auth Failure: AuthenticationManager rejected login for email: {}", loginRequest.email());
            throw new BadCredentialsException("Invalid Username or Password !!");
        }
    }
}