package com.project.auth_app_backend.services;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Getter
public class CookieService {

    private final String refreshTokenCookieName;
    private final boolean cookieHttpOnly;
    private final boolean cookieSecure;
    private final String cookieDomain;
    private final String cookieSameSite;
    private final String cookiePath;

    public CookieService(
            @Value("${security.jwt.refresh-token-cookie-name}") String refreshTokenCookieName,
            @Value("${security.jwt.cookie-http-only}") boolean cookieHttpOnly,
            @Value("${security.jwt.cookie-secure}") boolean cookieSecure,
            @Value("${security.jwt.cookie-same-site}") String cookieSameSite,
            @Value("${security.jwt.cookie-domain}") String cookieDomain,
            @Value("${security.jwt.cookie-path}") String cookiePath
    ) {
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieSecure = cookieSecure;
        this.cookieDomain = cookieDomain;
        this.cookieSameSite = cookieSameSite;
        this.cookiePath = cookiePath;
        
        log.info("CookieService initialized: Name={}, Secure={}, HttpOnly={}, SameSite={}", 
                refreshTokenCookieName, cookieSecure, cookieHttpOnly, cookieSameSite);
    }

    public void attachRefreshCookie(HttpServletResponse response, String value, int maxAge) {
        log.debug("Attaching Refresh Token cookie: [Name: {}, MaxAge: {}s, Path: {}]", 
                refreshTokenCookieName, maxAge, cookiePath);

        var responseCookieBuilder = ResponseCookie.from(refreshTokenCookieName, value)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path(cookiePath)
                .maxAge(maxAge)
                .sameSite(cookieSameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            log.trace("Setting cookie domain to: {}", cookieDomain);
            responseCookieBuilder.domain(cookieDomain);
        }

        ResponseCookie responseCookie = responseCookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
        
        this.addNoStoreHeaders(response);
    }

    public void clearRefreshCookie(HttpServletResponse response) {
        log.info("Clearing Refresh Token cookie: [Name: {}]", refreshTokenCookieName);

        var builder = ResponseCookie.from(refreshTokenCookieName, "")
                .maxAge(0) // Immediately expires the cookie
                .httpOnly(cookieHttpOnly)
                .path(cookiePath)
                .sameSite(cookieSameSite)
                .secure(cookieSecure);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        ResponseCookie responseCookie = builder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    public void addNoStoreHeaders(HttpServletResponse response) {
        log.trace("Adding Cache-Control: no-store headers to response");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader("Pragma", "no-cache");
    }
}