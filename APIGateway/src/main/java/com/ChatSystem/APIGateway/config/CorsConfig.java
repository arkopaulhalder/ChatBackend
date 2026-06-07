package com.ChatSystem.APIGateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS is configured ONCE here at the gateway.
 *
 * No downstream service (AuthService, ChatService, UserService) should have
 * its own CORS config. Client requests never reach those services directly —
 * they always go through the gateway, which applies this config first.
 *
 * Adding CORS config in AuthService as well would have no effect for real
 * client traffic (it never reaches AuthService without going through here),
 * and would only apply if someone bypassed the gateway entirely — which
 * should be blocked at the network level in production anyway.
 *
 * Before production:
 *   Replace allowedOrigins with your actual deployed frontend URL.
 *   Never use "*" with allowCredentials(true) — browsers reject that combination.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // TODO: replace with real frontend domain before deploying
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",   // React dev server
                "http://localhost:5173"    // Vite dev server
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-User-UUID",
                "X-User-Roles",
                "X-Correlation-ID",
                "X-User-Username"
        ));

        // Expose correlation ID so frontend can log it for debugging
        config.setExposedHeaders(List.of("X-Correlation-ID"));

        // Required for the frontend to send cookies or Authorization headers
        config.setAllowCredentials(true);

        // Browser caches preflight result for 1 hour — reduces OPTIONS request overhead
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}