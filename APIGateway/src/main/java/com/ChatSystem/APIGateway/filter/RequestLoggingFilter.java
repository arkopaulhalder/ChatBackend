package com.ChatSystem.APIGateway.filter;


import com.ChatSystem.common_library.constants.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter — runs on every request without being listed per-route.
 *
 * Logs: method, path, correlation-id, response status, and duration.
 * Keep log level at DEBUG in production to avoid noise.
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        String correlationId = exchange.getRequest().getHeaders().getFirst(AppConstants.CORRELATION_ID_HEADER);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            long duration = System.currentTimeMillis() - startTime;
            log.debug("[{}] {} {} → {} ({}ms)",
                    correlationId, method, path,
                    response.getStatusCode(), duration);
        }));
    }

    @Override
    public int getOrder() {
        // Run after CorrelationIdFilter (which is per-route) but before routing
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}

