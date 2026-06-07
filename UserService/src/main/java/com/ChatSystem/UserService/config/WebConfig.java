package com.ChatSystem.UserService.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Minimal web config for UserService.
 *
 * CORS is handled entirely at the APIGateway.
 * UserService should never be directly accessible from a browser —
 * all client traffic goes through the gateway.
 *
 * If you ever need to call UserService directly (e.g. from integration tests
 * or internal service-to-service calls), configure allowed origins here.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // No overrides needed for MVP.
    // Gateway handles CORS. UserService trusts gateway-injected headers.
}

