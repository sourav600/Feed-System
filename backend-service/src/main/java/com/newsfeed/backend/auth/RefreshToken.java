package com.newsfeed.backend.auth;

import com.newsfeed.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Server-side record backing the opaque refresh_token cookie. Only the SHA-256 hash of the raw
 * token is ever persisted; the raw value is never stored, only issued to the client once.
 */
@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, unique = true, columnDefinition = "CHAR(64)")
    private String tokenHash;

    @Column(nullable = false)
    private Instant issuedAt;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(64)")
    private String replacedByTokenHash;

    @Column(length = 255)
    private String userAgent;

    @Column(length = 45)
    private String ipAddress;

    /**
     * Holds the raw (unhashed) token only in-memory, only on the instance that just issued it -
     * never persisted, never read back from the DB. Lets {@link AuthService} set the cookie value
     * right after {@code save()} without threading the raw token through a second return value.
     */
    @Transient
    String rawTokenForCookieOnly;

    public boolean isActive(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
}
