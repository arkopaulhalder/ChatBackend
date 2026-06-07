package com.ChatSystem.ChatService.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class WebSocketEventListener {

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userUuid = accessor.getUser() != null ? accessor.getUser().getName() : "unknown";
        log.info("WebSocket connected: userUuid={} sessionId={}", userUuid, accessor.getSessionId());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userUuid = accessor.getUser() != null ? accessor.getUser().getName() : "unknown";
        log.info("WebSocket disconnected: userUuid={} sessionId={}", userUuid, accessor.getSessionId());
        // PresenceService handles the actual offline transition via Redis TTL expiry
    }
}
