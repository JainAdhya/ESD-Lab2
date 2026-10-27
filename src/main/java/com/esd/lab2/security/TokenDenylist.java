
package com.esd.lab2.security;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

@Component
public class TokenDenylist {

    private final ConcurrentMap<String, Instant> revokedTokens =
            new ConcurrentHashMap<>();

    /**
     * Store a revoked token ID and its expiration time.
     */
    public void revoke(String jti, Instant expiry) {
        if (jti == null || jti.isBlank()) {
            throw new IllegalArgumentException("Token ID cannot be blank");
        }

        if (expiry == null) {
            throw new IllegalArgumentException("Token expiry cannot be null");
        }

        // Don't store tokens that have already expired.
        if (expiry.isAfter(Instant.now())) {
            revokedTokens.put(jti, expiry);
        }
    }

    /**
     * Check whether a token ID has been revoked.
     */
    public boolean isRevoked(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }

        Instant expiry = revokedTokens.get(jti);

        if (expiry == null) {
            return false;
        }

        // Remove the entry if the token has naturally expired.
        if (!expiry.isAfter(Instant.now())) {
            revokedTokens.remove(jti, expiry);
            return false;
        }

        return true;
    }

    /**
     * Remove all expired token entries.
     * Can be called manually or scheduled later.
     */
    public void cleanExpired() {
        Instant now = Instant.now();

        revokedTokens.entrySet().removeIf(
                entry -> !entry.getValue().isAfter(now));
    }
}
