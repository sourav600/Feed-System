package com.newsfeed.backend.security;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

/**
 * Issues/clears the two httpOnly auth cookies. The CSRF cookie (XSRF-TOKEN, JS-readable by
 * design) is managed entirely by Spring Security's {@code CookieCsrfTokenRepository} - see
 * {@link SecurityConfig} - not here.
 */
@Service
@RequiredArgsConstructor
public class CookieService {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    public static final String REFRESH_TOKEN_PATH = "/api/auth/refresh";

    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;

    public void setAccessTokenCookie(HttpServletResponse response, String token) {
        addCookie(response, ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(cookieProperties.cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(jwtProperties.accessTokenTtlMinutes()))
                .build());
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        addCookie(response, ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(cookieProperties.cookieSecure())
                .sameSite("Strict")
                .path(REFRESH_TOKEN_PATH)
                .maxAge(Duration.ofDays(jwtProperties.refreshTokenTtlDays()))
                .build());
    }

    public void clearAuthCookies(HttpServletResponse response) {
        addCookie(response, ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true).secure(cookieProperties.cookieSecure()).sameSite("Lax").path("/").maxAge(0).build());
        addCookie(response, ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true).secure(cookieProperties.cookieSecure()).sameSite("Strict")
                .path(REFRESH_TOKEN_PATH).maxAge(0).build());
    }

    private void addCookie(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
