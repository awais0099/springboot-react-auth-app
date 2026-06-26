package com.project.auth_app_backend.security;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.project.auth_app_backend.entities.Provider;
import com.project.auth_app_backend.entities.RefreshToken;
import com.project.auth_app_backend.entities.User;
import com.project.auth_app_backend.repositories.RefreshTokenRepository;
import com.project.auth_app_backend.repositories.UserRepository;
import com.project.auth_app_backend.services.CookieService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.auth.frontend.success-redirect}")
    private String frontEndSuccessUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String registrationId = "unknown";

        if (authentication instanceof OAuth2AuthenticationToken token) {
            registrationId = token.getAuthorizedClientRegistrationId();
        }

        User user = null;

        // GOOGLE LOGIN
        if ("google".equals(registrationId)) {

            String googleId = oAuth2User.getAttribute("sub");
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String picture = oAuth2User.getAttribute("picture");

            // Check if user already exists
            Optional<User> optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            } else {
                User newUser = new User();

                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setImage(picture);
                newUser.setEnable(true);
                newUser.setProvider(Provider.GOOGLE);
                newUser.setProviderId(googleId);

                user = userRepository.save(newUser);
            }
        }

        // Safety check
        if (user == null) {
            throw new RuntimeException("OAuth2 user could not be processed");
        }

        // Create Refresh Token Entity
        String jti = UUID.randomUUID().toString();

        RefreshToken refreshTokenEntity = new RefreshToken();

        refreshTokenEntity.setJti(jti);
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setRevoked(false);
        refreshTokenEntity.setCreatedAt(Instant.now());

        refreshTokenEntity.setExpiresAt(
                Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds())
        );

        refreshTokenRepository.save(refreshTokenEntity);

        // Generate JWT Tokens
        String accessToken = jwtService.generateAccessToken(user);

        String refreshToken = jwtService.generateRefreshToken(
                user,
                refreshTokenEntity.getJti()
        );

        // Store refresh token in cookie
        cookieService.attachRefreshCookie(
                response,
                refreshToken,
                (int) jwtService.getRefreshTtlSeconds()
        );

        // Redirect
        response.getWriter().write("Login successful");
    }
}
