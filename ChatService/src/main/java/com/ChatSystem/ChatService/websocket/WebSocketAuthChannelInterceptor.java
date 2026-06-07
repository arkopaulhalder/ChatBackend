package com.ChatSystem.ChatService.websocket;

import com.ChatSystem.common_library.constants.AppConstants;
import com.ChatSystem.common_library.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * Validates the JWT token sent in the STOMP CONNECT frame's Authorization header.
 *
 * WebSocket connections are NOT routed through the API Gateway's HTTP filter chain —
 * the initial HTTP upgrade is proxied, but the STOMP session is independent.
 * This interceptor is therefore the correct place to authenticate WebSocket clients.
 *
 * After validation, sets the authenticated principal on the STOMP session
 * so Spring can route /user/queue/messages to the right user.
 */
@Slf4j
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader(AppConstants.AUTHORIZATION_HEADER);

            if (authHeader == null || !authHeader.startsWith(AppConstants.BEARER_PREFIX)) {
                log.warn("WebSocket CONNECT rejected — missing Authorization header");
                throw new IllegalStateException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(AppConstants.BEARER_PREFIX.length());

            if (!JwtUtils.isTokenValid(token, jwtSecret)) {
                log.warn("WebSocket CONNECT rejected — invalid or expired token");
                throw new IllegalStateException("Invalid or expired JWT token");
            }

            String userUuid = JwtUtils.extractUserUuid(token, jwtSecret);

            // Set the principal — Spring uses this to route /user/** destinations
            accessor.setUser(new StompPrincipal(userUuid));
            log.debug("WebSocket CONNECT authenticated for userUuid={}", userUuid);
        }

        return message;
    }

    /**
     * Minimal Principal implementation — Spring only needs getName() for routing.
     */
    private record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() { return name; }
    }
}
