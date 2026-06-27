package com.project.auth_app_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.auth_app_backend.dtos.ApiError;
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
            "/oauth2/authorization/**", 
            "/login/oauth2/code/**"      
    );

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Senior Optimization: Tells Spring Boot to natively bypass this filter for public endpoints.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestUri = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return PUBLIC_ENDPOINTS.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String header = request.getHeader(AUTHORIZATION_HEADER);

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            log.warn("JWT Filter: Missing or malformed Authorization token context for entry to: {}", requestURI);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Missing or malformed authorization credentials.", requestURI);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            log.warn("JWT Filter: Blank token string passed to endpoint: {}", requestURI);
            sendErrorResponse(response, HttpStatus.BAD_REQUEST, "Authorization token cannot be blank.", requestURI);
            return;
        }

        try {
            if (!jwtService.isAccessToken(token)) {
                log.warn("JWT Filter: Access verification failed. Invalid token type provided for: {}", requestURI);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token type context.", requestURI);
                return;
            }

            String email = jwtService.getEmail(token);
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                log.error("JWT Filter: Session rejected. User identity [{}] not active in database for: {}", email, requestURI);
                sendErrorResponse(response, HttpStatus.FORBIDDEN, "User account is suspended or invalid.", requestURI);
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
                log.debug("JWT Filter: Secure tunnel established for user: {}", email);
            }

        } catch (ExpiredJwtException exception) {
            log.warn("JWT Filter: Token lifecycle trace expired for path: {}", requestURI);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "The provided access token has expired.", requestURI);
            return;
        } catch (MalformedJwtException | SignatureException exception) {
            log.error("JWT Filter: Cryptographic signature mismatch detected for path: {}", requestURI);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid or altered cryptographic token structure.", requestURI);
            return;
        } catch (Exception exception) {
            log.error("JWT Filter: Critical unhandled failure inside authentication filter pipeline", exception);
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected system exception occurred.", requestURI);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        
        var apiError = new ApiError(status.value(), status.getReasonPhrase(), message, path, null);
        response.getWriter().write(objectMapper.writeValueAsString(apiError));
    }
}