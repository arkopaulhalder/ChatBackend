package com.ChatSystem.APIGateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Handles exceptions that escape the gateway filter chain.
 *
 * Covers:
 * - Service unavailable (503 when Eureka can't find the target service)
 * - Route not found (404)
 * - Rate limit exceeded (429)
 * - Any unhandled reactive exception
 *
 * Order(-1) ensures this runs before Spring Boot's default error handler.
 */
@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = resolveStatus(ex);
        String message = resolveMessage(ex, status);

        log.error("Gateway error [{}] {}: {}", status.value(),
                exchange.getRequest().getURI().getPath(), ex.getMessage());

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();

        try {
            byte[] body = objectMapper.writeValueAsBytes(Map.of(
                    "status", status.value(),
                    "error", status.getReasonPhrase(),
                    "message", message,
                    "path", exchange.getRequest().getURI().getPath(),
                    "timestamp", Instant.now().toString()
            ));
            return exchange.getResponse().writeWith(
                    Mono.just(bufferFactory.wrap(body))
            );
        } catch (JsonProcessingException e) {
            return exchange.getResponse().setComplete();
        }
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException rse) {
            return HttpStatus.valueOf(rse.getStatusCode().value());
        }
        // Service unreachable via Eureka
        if (ex.getMessage() != null && ex.getMessage().contains("Unable to find instance")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(Throwable ex, HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "Route not found";
            case UNAUTHORIZED -> "Authentication required";
            case FORBIDDEN -> "Access denied";
            case TOO_MANY_REQUESTS -> "Rate limit exceeded. Slow down.";
            case SERVICE_UNAVAILABLE -> "Service temporarily unavailable";
            default -> "An unexpected error occurred";
        };
    }
}

