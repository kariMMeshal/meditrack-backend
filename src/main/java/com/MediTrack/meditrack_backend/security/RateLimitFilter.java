// security/RateLimitFilter.java — full replacement
package com.MediTrack.meditrack_backend.security;

import io.github.bucket4j.*;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
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
import java.util.function.Supplier;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ProxyManager<byte[]> proxyManager;

    public RateLimitFilter(RedisClient redisClient) {
        this.proxyManager = LettuceBasedProxyManager
                .builderFor(redisClient)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = extractIp(request);
        String bucketKey = resolveBucketKey(uri, ip);
        Supplier<BucketConfiguration> configSupplier = resolveBucketConfig(uri);

        // Bucket is stored in Redis — shared across all app instances
        Bucket bucket = proxyManager.builder().build(bucketKey.getBytes(), configSupplier);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            logger.info("Remaining tokens: " + probe.getRemainingTokens());
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\"," +
                            "\"message\":\"Too many requests — please try again later\"}");
        }
    }

    private String resolveBucketKey(String uri, String ip) {
        // Separate buckets per IP per endpoint type
        if (uri.contains("/login"))   return "rate:login:"   + ip;
        if (uri.contains("/refresh")) return "rate:refresh:" + ip;
        return "rate:auth:" + ip;
    }

    private Supplier<BucketConfiguration> resolveBucketConfig(String uri) {
        int capacity = uri.contains("/login") ? 5 : uri.contains("/refresh") ? 20 : 30;
        return () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(capacity,
                        Refill.intervally(capacity, Duration.ofMinutes(1))))
                .build();
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}