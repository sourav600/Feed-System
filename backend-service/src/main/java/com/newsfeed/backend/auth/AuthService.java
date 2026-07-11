package com.newsfeed.backend.auth;

import com.newsfeed.backend.common.exception.ConflictException;
import com.newsfeed.backend.security.CookieService;
import com.newsfeed.backend.security.JwtProperties;
import com.newsfeed.backend.security.JwtService;
import com.newsfeed.backend.user.User;
import com.newsfeed.backend.user.UserService;
import com.newsfeed.backend.user.dto.CurrentUserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final CookieService cookieService;
    private final RefreshTokenRepository refreshTokenRepository;

    public CurrentUserResponse register(String firstName, String lastName, String email, String rawPassword,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (userService.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists.");
        }
        User user = userService.create(User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .build());

        issueTokens(user, request, response);
        return CurrentUserResponse.from(user);
    }

    public CurrentUserResponse login(String email, String rawPassword,
                                      HttpServletRequest request, HttpServletResponse response) {
        User user = userService.findByEmailOrNull(email);
        // Same generic message whether the email doesn't exist or the password is wrong - never
        // reveal which one was incorrect (avoids leaking valid account emails via login errors).
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password.");
        }
        issueTokens(user, request, response);
        return CurrentUserResponse.from(user);
    }

    /** Rotates the refresh token: issues a fresh pair and revokes the presented one. */
    public void refresh(String rawRefreshToken, HttpServletRequest request, HttpServletResponse response) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new BadCredentialsException("Missing refresh token.");
        }
        String hash = sha256Hex(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token."));

        if (!existing.isActive(Instant.now())) {
            throw new BadCredentialsException("Refresh token is expired or has been revoked.");
        }

        User user = existing.getUser();
        RefreshToken rotated = issueRefreshToken(user, request);
        existing.setRevokedAt(Instant.now());
        existing.setReplacedByTokenHash(rotated.getTokenHash());

        cookieService.setAccessTokenCookie(response, jwtService.generateAccessToken(user.getId()));
        cookieService.setRefreshTokenCookie(response, rotated.rawTokenForCookieOnly);
    }

    public void logout(String rawRefreshToken, HttpServletResponse response) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenRepository.findByTokenHash(sha256Hex(rawRefreshToken))
                    .ifPresent(token -> token.setRevokedAt(Instant.now()));
        }
        cookieService.clearAuthCookies(response);
    }

    private void issueTokens(User user, HttpServletRequest request, HttpServletResponse response) {
        RefreshToken refreshToken = issueRefreshToken(user, request);
        cookieService.setAccessTokenCookie(response, jwtService.generateAccessToken(user.getId()));
        cookieService.setRefreshTokenCookie(response, refreshToken.rawTokenForCookieOnly);
    }

    private RefreshToken issueRefreshToken(User user, HttpServletRequest request) {
        String rawToken = generateOpaqueToken();
        Instant now = Instant.now();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(sha256Hex(rawToken))
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofDays(jwtProperties.refreshTokenTtlDays())))
                .userAgent(truncate(request.getHeader("User-Agent"), 255))
                .ipAddress(truncate(request.getRemoteAddr(), 45))
                .build();
        refreshTokenRepository.save(refreshToken);
        refreshToken.rawTokenForCookieOnly = rawToken;
        return refreshToken;
    }

    private static String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
