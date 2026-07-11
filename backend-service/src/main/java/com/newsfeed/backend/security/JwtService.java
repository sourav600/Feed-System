package com.newsfeed.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Signs and validates the short-lived access token JWT. Claims are deliberately minimal (subject
 * + standard timestamps only, no PII) - author/display info in responses always comes from a DB
 * join, never from decoding the token.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofMinutes(properties.accessTokenTtlMinutes()));
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key())
                .compact();
    }

    /** Returns the userId if the token is structurally valid, signed correctly, and not expired. */
    public Optional<Long> parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(Long.valueOf(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long accessTokenTtlSeconds() {
        return Duration.ofMinutes(properties.accessTokenTtlMinutes()).toSeconds();
    }
}
