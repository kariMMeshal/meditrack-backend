package com.MediTrack.meditrack_backend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate-limits authentication endpoints to prevent brute force attacks.
 *
 * Strategy: per-IP token bucket
 *   - /api/auth/login    → 10 requests per minute
 *   - /api/auth/refresh  → 20 requests per minute
 *   - Other /api/auth/** → 30 requests per minute
 *
 * Buckets are stored in-memory (ConcurrentHashMap).
 * For multi-instance deployments, replace with Redis-backed Bucket4j.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int LOGIN_CAPACITY   = 10;
    private static final int REFRESH_CAPACITY = 20;
    private static final int AUTH_CAPACITY    = 30;

    private final Map<String, Bucket> loginBuckets   = new ConcurrentHashMap<>();
    private final Map<String, Bucket> refreshBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> authBuckets    = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // Only rate-limit auth endpoints
        if (!uri.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = extractIp(request);
        Bucket bucket = resolveBucket(uri, ip);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\"," +
                            "\"message\":\"Too many requests — please try again later\"}");
        }
    }

    private Bucket resolveBucket(String uri, String ip) {
        if (uri.contains("/login")) {
            return loginBuckets.computeIfAbsent(ip, k -> buildBucket(LOGIN_CAPACITY));
        }
        if (uri.contains("/refresh")) {
            return refreshBuckets.computeIfAbsent(ip, k -> buildBucket(REFRESH_CAPACITY));
        }
        return authBuckets.computeIfAbsent(ip, k -> buildBucket(AUTH_CAPACITY));
    }

    private Bucket buildBucket(int requestsPerMinute) {
        Bandwidth limit = Bandwidth.classic(
                requestsPerMinute,
                Refill.intervally(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}