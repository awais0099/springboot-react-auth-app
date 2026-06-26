package com.project.auth_app_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.auth_app_backend.dtos.ErrorResponse;
import com.project.auth_app_backend.entities.User;
import com.project.auth_app_backend.repositories.UserRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/**",
            "/favicon.ico",
            "/.well-known/**",
            "/oauth2/authorization/**", // 1. The trigger URL you just hit
            "/login/oauth2/code/**"      // 2. The callback URL Google sends you back to
    );

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.debug("JWT Filter: Incoming request [{} {}]", method, requestURI);

        log.info("requestURI: " + requestURI);
        if (isPublicEndpoint(requestURI)) {
            log.debug("JWT Filter: Public endpoint detected for {}. Skipping authentication.", requestURI);
            filterChain.doFilter(request, response);
            return;
        } else {
        	log.debug("fuk: " + requestURI);
        }

        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            log.warn("JWT Filter: Missing or invalid Authorization header for URI: {}", requestURI);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid or missing Authorization header. Expected format: 'Bearer <token>'");
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            log.warn("JWT Filter: Bearer token is empty for URI: {}", requestURI);
            sendErrorResponse(response, HttpStatus.BAD_REQUEST, "Token is empty");
            return;
        }

        try {
            // Validate token type
            if (!jwtService.isAccessToken(token)) {
                log.warn("JWT Filter: Token provided is not an Access Token for URI: {}", requestURI);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token type");
                return;
            }


            String email = jwtService.getEmail(token);
            log.debug("JWT Filter: Validating session for user: {}", email);

            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                log.error("JWT Filter: Token is valid but user {} does not exist in DB for URI: {}", email, requestURI);
                sendErrorResponse(response, HttpStatus.FORBIDDEN, "User account is disabled or not found");
                return;
            }

            List<String> rolesStr = jwtService.getRoles(token);
            List<GrantedAuthority> roles = rolesStr.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    roles
            );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("JWT Filter: Successfully authenticated user {} for URI: {}", email, requestURI);
            }

        } catch (ExpiredJwtException exception) {
            log.warn("JWT Filter: Expired token for URI: {} | Message: {}", requestURI, exception.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token has expired");
            return;
        } catch (MalformedJwtException | SignatureException exception) {
            log.error("JWT Filter: Potential tampering or invalid format detected: {}", exception.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token format");
            return;
        } catch (Exception exception) {
            log.error("JWT Filter: UNEXPECTED SYSTEM ERROR during authentication for URI: {}", requestURI, exception);
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        // Logging the response we are sending back to the client
        log.debug("JWT Filter: Sending error response [{}] - {}", status.value(), message);
        
        response.setStatus(status.value());
        response.setContentType("application/json");
        ErrorResponse errorResponse = new ErrorResponse(status, status.value(), message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private boolean isPublicEndpoint(String requestUri) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        
        boolean isPublic = PUBLIC_ENDPOINTS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));

        if (isPublic) {
            log.trace("Security Filter: Skipping JWT validation for public endpoint: {}", requestUri);
        }

        return isPublic;
    }
}