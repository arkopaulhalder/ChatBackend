package com.ChatSystem.APIGateway.filter;

import com.ChatSystem.common_library.constants.AppConstants;
import com.ChatSystem.APIGateway.security.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Applied per-route in application.yml via the name "JwtAuthenticationFilter".
 *
 * This filter IS the security layer for the gateway. There is no Spring Security here.
 *
 * What it does:
 *   1. Checks if the route is public — if yes, skip validation and forward
 *   2. Reads Authorization: Bearer <token> header
 *   3. Validates JWT signature and expiry via JwtTokenValidator
 *   4. Extracts userUuid, username, roles from token claims
 *   5. Strips the raw Authorization header (downstream services must never see it)
 *   6. Injects X-User-UUID, X-User-Roles, X-User-Username headers
 *   7. Forwards the mutated request to the target service
 *
 * Public paths (no JWT required):
 *   /auth/register, /auth/login, /auth/refresh, /actuator/health
 *
 * Everything else requires a valid JWT.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    // Paths that do not require a JWT token.
    // Keep this list minimal. Every path not listed here is protected.
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/register",
            "/auth/login",
            "/auth/refresh",
            "/actuator/health"
    );

    private final JwtTokenValidator tokenValidator;

    public JwtAuthenticationFilter(JwtTokenValidator tokenValidator) {
        super(Config.class);
        this.tokenValidator = tokenValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // Public route — forward without any token check
            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith(AppConstants.BEARER_PREFIX)) {
                log.debug("Missing or malformed Authorization header for path: {}", path);
                return reject(exchange, HttpStatus.UNAUTHORIZED, "Authorization header missing");
            }

            String token = authHeader.substring(AppConstants.BEARER_PREFIX.length());

            if (!tokenValidator.isValid(token)) {
                log.debug("Invalid or expired JWT for path: {}", path);
                return reject(exchange, HttpStatus.UNAUTHORIZED, "Token invalid or expired");
            }

            String userUuid = tokenValidator.extractUserUuid(token);
            String username  = tokenValidator.extractUsername(token);
            List<String> roles = tokenValidator.extractRoles(token);
            String rolesHeader = String.join(",", roles);

            // Mutate the forwarded request:
            //   - Remove raw JWT (downstream services must not parse tokens themselves)
            //   - Add verified identity headers (downstream services trust these)
            ServerWebExchange mutated = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        headers.remove(HttpHeaders.AUTHORIZATION);
                        headers.set(AppConstants.USER_UUID_HEADER,   userUuid);
                        headers.set(AppConstants.USER_ROLES_HEADER,  rolesHeader);
                        headers.set("X-User-Username",               username);
                    }))
                    .build();

            log.debug("JWT valid — forwarding userUuid={} path={}", userUuid, path);
            return chain.filter(mutated);
        };
    }

    private boolean isPublicPath(String                     path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String reason) {
        log.warn("Request rejected [{}]: {} — {}", status.value(),
                exchange.getRequest().getURI().getPath(), reason);
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Reserved for future per-route config (e.g. required roles per route).
    }
}