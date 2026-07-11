package com.newsfeed.gateway.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Implemented as a plain servlet filter (not a Spring-Cloud-Gateway-native filter function) so it
 * doesn't depend on that module's newer, less-stable extension SPI - a gateway-service is still
 * an ordinary Spring MVC app under the routing layer, so a standard {@link OncePerRequestFilter}
 * intercepts every request before it's proxied, which is all "rate limit before it reaches the
 * backend" actually requires.
 *
 * <p>In-memory per-IP token buckets are sufficient for a single gateway instance (documented as a
 * future Redis-backed swap once running >1 instance - see the plan's "Future Scalability"
 * section). Two scopes are limited: login (brute-force protection) and post creation (spam
 * protection); everything else passes through unmetered.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;

    private final ConcurrentHashMap<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> postBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = clientIp(request);

        Bucket bucket = null;
        if ("POST".equalsIgnoreCase(method) && path.equals("/api/auth/login")) {
            bucket = loginBuckets.computeIfAbsent(clientIp, ip -> newBucket(properties.login()));
        } else if ("POST".equalsIgnoreCase(method) && path.equals("/api/posts")) {
            bucket = postBuckets.computeIfAbsent(clientIp, ip -> newBucket(properties.posts()));
        }

        if (bucket != null && !bucket.tryConsume(1)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Too many requests. Please slow down and try again shortly.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private static Bucket newBucket(RateLimitProperties.BucketSpec spec) {
        Bandwidth limit = Bandwidth.classic(
                spec.capacity(),
                Refill.greedy(spec.refillTokens(), Duration.ofSeconds(spec.refillPeriodSeconds())));
        return Bucket.builder().addLimit(limit).build();
    }

    private static String clientIp(HttpServletRequest request) {
        // Deliberately NOT trusting X-Forwarded-For: this gateway is the outermost edge in this
        // architecture (no LB/CDN in front of it - see the plan's "Gateway replaces the Load
        // Balancer" decision), so that header is fully client-controlled and trusting it would let
        // an attacker spoof a fresh IP on every request to bypass the rate limit entirely. Revisit
        // only once a trusted reverse proxy actually sits in front of this service.
        return request.getRemoteAddr();
    }
}
