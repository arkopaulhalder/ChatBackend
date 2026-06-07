package com.ChatSystem.APIGateway.config;


import com.ChatSystem.APIGateway.security.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Rate limiting uses Redis token bucket algorithm.
 *
 * Two limiters:
 *  - authRateLimiter: tight — 5 req/sec, burst 10. Applied to /auth/login to block brute force.
 *  - defaultRateLimiter: loose — 50 req/sec, burst 100. Applied globally.
 *
 * Key is the client IP address. For mobile clients behind NAT, this means the whole
 * NAT group shares a bucket — acceptable for MVP. Post-MVP: key by user UUID when available.
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class RedisRateLimiterConfig {

    private final JwtTokenValidator tokenValidator;

    /**
     * Primary key resolver — used by default when no key-resolver is specified.
     *
     * Strategy:
     *  1. Try to extract userUuid from the JWT token directly
     *     — token is already validated by JwtAuthenticationFilter at this point
     *     — UUID comes from the cryptographically signed token, not a client header
     *     — client cannot forge this
     *  2. Fall back to IP address if no token present
     *     — covers /auth/login, /auth/register (unauthenticated endpoints)
     *
     * Why NOT read X-User-UUID header:
     *  — X-User-UUID is injected by JwtAuthenticationFilter AFTER rate limiting runs
     *  — Even if it were available, a client could send a forged X-User-UUID
     *    before it reaches the gateway filter chain
     *  — JWT extraction is the only tamper-proof source of user identity
     */

    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Try to get identity from JWT token — tamper-proof, cryptographically signed
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    if (tokenValidator.isValid(token)) {
                        String userUuid = tokenValidator.extractUserUuid(token);
                        if (userUuid != null && !userUuid.isBlank()) {
                            // Rate limit by verified user identity
                            return Mono.just("user:" + userUuid);
                        }
                    }
                } catch (Exception e) {
                    // Token malformed or expired — fall through to IP-based limiting
                    log.debug("Could not extract UUID from token for rate limiting: {}", e.getMessage());
                }
            }

            // No valid token — rate limit by IP address
            // Covers: /auth/login, /auth/register, /auth/refresh
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just("ip:" + ip);
        };
    }

    /**
     * Tight limiter for login endpoint — prevents brute force attacks.
     * replenishRate: tokens added per second
     * burstCapacity: max tokens in the bucket at any time
     */
    @Bean
    @Primary
    public RedisRateLimiter authRateLimiter() {
        return new RedisRateLimiter(5, 10);
    }

    /**
     * Default limiter for all other routes.
     */
    @Bean
    public RedisRateLimiter defaultRateLimiter() {
        return new RedisRateLimiter(50, 100);
    }
}

