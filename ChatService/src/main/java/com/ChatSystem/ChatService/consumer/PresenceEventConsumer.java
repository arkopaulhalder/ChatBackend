package com.ChatSystem.ChatService.consumer;


import com.ChatSystem.common_library.constants.KafkaTopics;
import com.ChatSystem.common_library.event.PresenceUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumes presence.updated events from PresenceService
 * and broadcasts them via WebSocket to connected clients.
 *
 * This is how clients know a contact came online or went offline
 * without polling the presence endpoint.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceEventConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = KafkaTopics.PRESENCE_UPDATED,
            groupId = "chat-service-presence-group",
            containerFactory = "presenceKafkaListenerContainerFactory"
    )
    public void consumePresenceUpdate(PresenceUpdatedEvent event) {
        log.debug("Presence update received: userUuid={} status={}", event.getUserUuid(), event.getStatus());

        // Broadcast to any client subscribed to this user's presence topic
        messagingTemplate.convertAndSend(
                "/topic/presence." + event.getUserUuid(),
                event
        );
    }
}

