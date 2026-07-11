package com.newsfeed.backend.common.pagination;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Opaque keyset-pagination cursor encoding a {@code (createdAt, id)} position.
 *
 * <p>Deliberately encodes ONLY positional state — never visibility or viewer identity — so the
 * same cursor format is safe to reuse across different viewers. The caller's visibility predicate
 * is re-applied fresh on every page fetch; the cursor only ever narrows the {@code <} boundary
 * inside an already-filtered query. See {@code PostRepositoryImpl} for the query that consumes this.
 */
public record Cursor(Instant createdAt, Long id) {

    public String encode() {
        String raw = createdAt.toString() + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static Cursor decode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
            int sep = raw.lastIndexOf('|');
            if (sep < 0) {
                throw new IllegalArgumentException("Malformed cursor");
            }
            Instant createdAt = Instant.parse(raw.substring(0, sep));
            Long id = Long.parseLong(raw.substring(sep + 1));
            return new Cursor(createdAt, id);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid pagination cursor.");
        }
    }
}
