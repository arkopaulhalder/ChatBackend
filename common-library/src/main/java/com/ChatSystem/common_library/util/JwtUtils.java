package com.ChatSystem.common_library.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Stateless JWT utility. No Spring beans — safe to use anywhere.
 *
 * The secret must be at least 32 characters (256-bit for HMAC-SHA256).
 * Inject it from configuration; never hardcode it.
 */
public final class JwtUtils {

    private JwtUtils() {}

    public static SecretKey buildKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public static Claims parseAllClaims(String token, String secret) {
        return Jwts.parser()
                .verifyWith(buildKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static String extractUserUuid(String token, String secret) {
        return parseAllClaims(token, secret).getSubject();
    }

    public static String extractUsername(String token, String secret) {
        return parseAllClaims(token, secret).get("username", String.class);
    }

    @SuppressWarnings("unchecked")
    public static List<String> extractRoles(String token, String secret) {
        return parseAllClaims(token, secret).get("roles", List.class);
    }

    public static boolean isTokenExpired(String token, String secret) {
        Date expiration = parseAllClaims(token, secret).getExpiration();
        return expiration.before(new Date());
    }

    /**
     * Returns true if the token is valid (signature ok AND not expired).
     * Swallows JwtException to make it safe for filter use.
     */
    public static boolean isTokenValid(String token, String secret) {
        try {
            return !isTokenExpired(token, secret);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
