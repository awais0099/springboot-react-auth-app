package com.project.auth_app_backend.controllers;

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
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final ModelMapper mapper;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        log.info("Auth Entry: Registration attempt for email: {}", userDto.getEmail());
        UserDto savedUserDto = authService.registerUser(userDto);
        log.info("Auth Success: User registered with ID: {}", savedUserDto.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUserDto);
    }

    @PostMapping("/login")
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

        // REUSE DETECTION LOGGING
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
        
        // Attach new cookie
        cookieService.attachRefreshCookie(response, newRefreshToken, (int) jwtService.getRefreshTtlSeconds());

        return ResponseEntity.ok(new TokenResponse(newAccessToken, newRefreshToken, jwtService.getAccessTtlSeconds(), "refresh", mapper.map(user, UserDto.class)));
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

        log.trace("Utility: No Refresh Token found in cookies or body.");
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

    @PostMapping("/logout")
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

        // Blacklist Access Token
		/*
		 * String authHeader = request.getHeader("Authorization"); if (authHeader !=
		 * null && authHeader.startsWith("Bearer ")) { String accessToken =
		 * authHeader.substring(7); try { String jti = jwtService.getJti(accessToken);
		 * Date expiration = jwtService.extractExpiration(accessToken);
		 * blacklistService.blacklistToken(jti, expiration);
		 * log.debug("Auth Success: Access token JTI {} blacklisted.", jti); } catch
		 * (Exception e) {
		 * log.warn("Auth Warning: Could not blacklist access token during logout."); }
		 * }
		 */

        cookieService.clearRefreshCookie(response);
        SecurityContextHolder.clearContext();
        
        log.info("Auth Success: User logged out successfully.");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}