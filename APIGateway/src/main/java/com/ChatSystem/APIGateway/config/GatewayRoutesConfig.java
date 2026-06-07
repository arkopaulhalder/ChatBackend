package com.ChatSystem.APIGateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Programmatic routes defined here override or supplement application.yml routes.
 *
 * For MVP, routes are primarily in application.yml for clarity.
 * This class is the right place to add complex predicate logic that YAML can't express,
 * such as header-based routing or request body inspection.
 *
 * Currently this only adds the stricter rate limiter to the login endpoint specifically.
 */
@Configuration
public class GatewayRoutesConfig {

    private final RedisRateLimiterConfig rateLimiterConfig;

    public GatewayRoutesConfig(RedisRateLimiterConfig rateLimiterConfig) {
        this.rateLimiterConfig = rateLimiterConfig;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // Override login route with the tighter rate limiter
                .route("auth-login-rate-limited", r -> r
                        .path("/auth/login")
                        .filters(f -> f
                                .requestRateLimiter(c -> {
                                    c.setRateLimiter(rateLimiterConfig.authRateLimiter());
                                    c.setKeyResolver(rateLimiterConfig.userKeyResolver());
                                })
                        )
                        .uri("lb://auth-service")
                )

                .build();
    }
}
