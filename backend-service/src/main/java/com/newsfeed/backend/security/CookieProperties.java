package com.newsfeed.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code app.security.cookie-secure} defaults to true (cookies only sent over HTTPS). Only flip
 * to false for plain-HTTP local development - never in a deployed environment.
 */
@ConfigurationProperties(prefix = "app.security")
public record CookieProperties(boolean cookieSecure) {

    public CookieProperties {
        // Records don't support @ConfigurationProperties default values via the canonical
        // constructor directly, so application.yml always supplies this explicitly (see below).
    }
}
