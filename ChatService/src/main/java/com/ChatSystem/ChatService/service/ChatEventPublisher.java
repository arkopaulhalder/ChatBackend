package com.ChatSystem.ChatService.service;


import com.ChatSystem.common_library.constants.KafkaTopics;
import com.ChatSystem.common_library.event.MessageCreatedEvent;
import com.ChatSystem.common_library.event.MessageDeliveredEvent;
import com.ChatSystem.common_library.event.MessageSeenEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishMessageCreated(MessageCreatedEvent event) {
        kafkaTemplate.send(KafkaTopics.MESSAGE_CREATED, event.getConversationId(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.error("Failed to publish MessageCreatedEvent: {}", ex.getMessage());
                });
    }

    public void publishMessageDelivered(MessageDeliveredEvent event) {
        kafkaTemplate.send(KafkaTopics.MESSAGE_DELIVERED, event.getMessageId(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.error("Failed to publish MessageDeliveredEvent: {}", ex.getMessage());
                });
    }

    public void publishMessageSeen(MessageSeenEvent event) {
        kafkaTemplate.send(KafkaTopics.MESSAGE_SEEN, event.getMessageId(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.error("Failed to publish MessageSeenEvent: {}", ex.getMessage());
                });
    }
}
