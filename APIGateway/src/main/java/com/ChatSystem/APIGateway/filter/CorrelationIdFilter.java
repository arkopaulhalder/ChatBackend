package com.ChatSystem.APIGateway.filter;

import com.ChatSystem.common_library.constants.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Assigns a unique correlation ID to every request.
 *
 * If the client provides X-Correlation-ID, we keep it (useful for frontend debugging).
 * If not, we generate one.
 *
 * The ID propagates to all downstream services so a single client request
 * can be traced across AuthService → ChatService → PresenceService logs.
 */
@Slf4j
@Component
public class CorrelationIdFilter extends AbstractGatewayFilterFactory<CorrelationIdFilter.Config> {

    public CorrelationIdFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String correlationId = exchange.getRequest().getHeaders()
                    .getFirst(AppConstants.CORRELATION_ID_HEADER);

            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            final String finalCorrelationId = correlationId;

            // Inject the correlation ID into the forwarded request headers
            var mutatedExchange = exchange.mutate()
                    .request(r -> r.headers(h -> h.set(AppConstants.CORRELATION_ID_HEADER, finalCorrelationId)))
                    .build();

            // After the downstream response comes back, add the same ID to the response headers
            // so the frontend/client can read it for debugging
            mutatedExchange.getResponse().getHeaders()
                    .set(AppConstants.CORRELATION_ID_HEADER, finalCorrelationId);

            return chain.filter(mutatedExchange);
        };
    }

    public static class Config {}
}

