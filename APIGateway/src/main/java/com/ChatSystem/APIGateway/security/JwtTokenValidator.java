package com.ChatSystem.APIGateway.security;

import com.ChatSystem.common_library.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Stateless JWT validator used exclusively by the gateway.
 *
 * This class only VALIDATES and READS tokens — it never issues them.
 * Token issuance belongs to AuthService.JwtService.
 *
 * No UserDetailsService, no database calls, no Spring Security authentication.
 * The token carries all the identity information we need.
 */
@Slf4j
@Component
public class JwtTokenValidator {

    @Value("${jwt.secret}")
    private String secret;

    public boolean isValid(String token) {
        try {
            return JwtUtils.isTokenValid(token, secret);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUserUuid(String token) {
        return JwtUtils.extractUserUuid(token, secret);
    }

    public String extractUsername(String token) {
        return JwtUtils.extractUsername(token, secret);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return JwtUtils.extractRoles(token, secret);
    }

    public Claims extractAllClaims(String token) {
        return JwtUtils.parseAllClaims(token, secret);
    }
}

