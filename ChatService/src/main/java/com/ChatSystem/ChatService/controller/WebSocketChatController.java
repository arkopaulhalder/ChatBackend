package com.ChatSystem.ChatService.controller;

import com.ChatSystem.ChatService.dto.MessageResponse;
import com.ChatSystem.ChatService.dto.SendMessageRequest;
import com.ChatSystem.ChatService.dto.TypingEventRequest;
import com.ChatSystem.ChatService.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * Handles STOMP messages from clients over WebSocket.
 *
 * Client sends to:  /app/chat.send     → message sent to conversation
 * Client sends to:  /app/chat.typing   → typing indicator broadcast
 *
 * Server pushes to: /topic/conversation.{id}  → all participants
 * Server pushes to: /user/queue/messages       → specific user (multi-device)
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request, Principal principal) {
        String senderUuid = principal.getName();
        // Delegate to MessageService which handles persistence and WS broadcast
        MessageResponse response = messageService.sendMessage(request, senderUuid);
        log.debug("WebSocket message processed: messageId={}", response.getMessageId());
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingEventRequest request, Principal principal) {
        String senderUuid = principal.getName();
        // Broadcast typing indicator to all conversation subscribers
        messagingTemplate.convertAndSend(
                "/topic/conversation." + request.getConversationId() + ".typing",
                Map.of(
                        "userUuid", senderUuid,
                        "typing", request.isTyping()
                )
        );
    }
}