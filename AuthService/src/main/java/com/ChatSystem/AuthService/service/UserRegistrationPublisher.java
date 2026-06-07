package com.ChatSystem.AuthService.service;

import com.ChatSystem.common_library.constants.KafkaTopics;
import com.ChatSystem.common_library.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistrationPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish a user.created event so UserService can create the profile.
     * Use userUuid as the Kafka message key for partition affinity.
     */
    public void publish(UserCreatedEvent event) {
        kafkaTemplate.send(KafkaTopics.USER_CREATED, event.getUserUuid(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        // Log and move on — registration succeeded DB-side.
                        // UserService can handle missing profiles gracefully.
                        log.error("Failed to publish UserCreatedEvent for uuid={}: {}",
                                event.getUserUuid(), ex.getMessage());
                    } else {
                        log.info("Published UserCreatedEvent for uuid={} to topic={}",
                                event.getUserUuid(), KafkaTopics.USER_CREATED);
                    }
                });
    }
}

