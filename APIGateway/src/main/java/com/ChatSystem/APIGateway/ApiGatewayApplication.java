package com.ChatSystem.APIGateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway — entry point for all client traffic.
 *
 * Responsibilities handled here:
 *   - JWT validation (JwtAuthenticationFilter)
 *   - CORS (CorsConfig)
 *   - Rate limiting (RedisRateLimiterConfig)
 *   - Request routing (application.yml routes)
 *   - Correlation ID injection (CorrelationIdFilter)
 *   - Request logging (RequestLoggingFilter)
 *
 * Spring Security is intentionally NOT on the classpath.
 * Security is handled by the custom filter chain above.
 * No exclusion annotations needed — the dependency simply isn't here.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}
}
