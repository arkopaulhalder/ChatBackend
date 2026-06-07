package com.ChatSystem.UserService.service;


import com.ChatSystem.common_library.constants.KafkaTopics;
import com.ChatSystem.common_library.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes the user.created event published by AuthService after registration.
 *
 * This is the only way UserService learns about new users — it does NOT
 * call AuthService over HTTP. The Kafka event carries enough data to
 * create the initial profile (uuid, username, email, phone).
 *
 * Idempotency: UserProfileService.createProfileFromEvent() is a no-op
 * if the profile already exists, so duplicate events are safe.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedEventConsumer {

    private final UserProfileService userProfileService;

    @KafkaListener(
            topics = KafkaTopics.USER_CREATED,
            groupId = "user-service-group",
            containerFactory = "userCreatedKafkaListenerContainerFactory"
    )
    public void consume(UserCreatedEvent event) {
        log.info("Received user.created event for userUuid={}", event.getUserUuid());
        try {
            userProfileService.createProfileFromEvent(event);
        } catch (Exception e) {
            // Log and allow Kafka to continue — a failed profile creation should not
            // block other messages. In production, route to a dead-letter topic here.
            log.error("Failed to create profile for userUuid={}: {}", event.getUserUuid(), e.getMessage(), e);
        }
    }
}

