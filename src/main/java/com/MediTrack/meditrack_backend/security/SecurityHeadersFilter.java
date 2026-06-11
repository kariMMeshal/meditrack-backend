package com.MediTrack.meditrack_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adds production security headers to every HTTPS response.
 *
 * Headers applied:
 *   - Strict-Transport-Security  → enforces HTTPS
 *   - X-Content-Type-Options     → prevents MIME sniffing
 *   - X-Frame-Options            → prevents clickjacking
 *   - X-XSS-Protection           → legacy XSS filter hint
 *   - Referrer-Policy            → controls referrer leakage
 *   - Content-Security-Policy    → restricts resource origins
 *   - Cache-Control              → prevents caching of sensitive API responses
 *   - Permissions-Policy         → disables unused browser features
 */
@Component
@Order(1)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Force HTTPS for 1 year, include subdomains
        response.setHeader("Strict-Transport-Security",
                "max-age=31536000; includeSubDomains; preload");

        // Prevent MIME type sniffing — browser must respect declared Content-Type
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Prevent this page from being embedded in frames (clickjacking protection)
        response.setHeader("X-Frame-Options", "DENY");

        // Legacy XSS filter — modern browsers use CSP instead
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Don't send Referer header when navigating away from this origin
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        String path = request.getRequestURI();

        boolean isSwagger = path.startsWith("/swagger-ui") ||
                            path.startsWith("/v3/api-docs") ||
                            path.startsWith("/swagger-ui.html");
        if (isSwagger) {
            response.setHeader("Content-Security-Policy",
                    "default-src 'self'; " +
                            "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                            "style-src 'self' 'unsafe-inline'; " +
                            "img-src 'self' data:; " +
                            "connect-src 'self';");
        } else {
            response.setHeader("Content-Security-Policy",
                    "default-src 'none'; " +
                            "frame-ancestors 'none'; " +
                            "base-uri 'none'; " +
                            "form-action 'self';");
        }

        // Prevent browsers from caching API responses
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, private");
        response.setHeader("Pragma", "no-cache");

        // Disable unused browser features
        response.setHeader("Permissions-Policy",
                "camera=(), microphone=(), geolocation=(), payment=()");

        filterChain.doFilter(request, response);
    }
}