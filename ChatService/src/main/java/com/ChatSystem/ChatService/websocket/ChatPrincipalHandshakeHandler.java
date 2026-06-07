package com.ChatSystem.ChatService.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/**
 * Assigns a temporary anonymous principal during the HTTP upgrade handshake.
 * The real authentication happens in WebSocketAuthChannelInterceptor on STOMP CONNECT.
 */
@Component
public class ChatPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        // Temporary — replaced by the authenticated principal in the STOMP CONNECT interceptor
        String sessionId = UUID.randomUUID().toString();
        return () -> "anon-" + sessionId;
    }
}
