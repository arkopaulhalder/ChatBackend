package com.ChatSystem.ChatService.config;


import com.ChatSystem.ChatService.websocket.WebSocketAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthChannelInterceptor authChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory broker for a topic and user-specific queues
        registry.enableSimpleBroker("/topic", "/user");

        // Prefix for messages sent from clients to @MessageMapping handlers
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific destinations (/user/queue/messages)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")  // Restrict to real origins before production
                .withSockJS();                  // SockJS fallback for environments that block WS
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // The auth interceptor validates the JWT token sent in the STOMP CONNECT frame
        registration.interceptors(authChannelInterceptor);
    }
}
