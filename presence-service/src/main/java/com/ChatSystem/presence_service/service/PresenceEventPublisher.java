package com.ChatSystem.presence_service.service;


import com.ChatSystem.common_library.constants.KafkaTopics;
import com.ChatSystem.common_library.enums.UserStatus;
import com.ChatSystem.common_library.event.PresenceUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPresenceUpdate(String userUuid, UserStatus status) {
        PresenceUpdatedEvent event = PresenceUpdatedEvent.builder()
                .userUuid(userUuid)
                .status(status)
                .build();

        kafkaTemplate.send(KafkaTopics.PRESENCE_UPDATED, userUuid, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish presence update for userUuid={}: {}", userUuid, ex.getMessage());
                    } else {
                        log.debug("Presence update published: userUuid={} status={}", userUuid, status);
                    }
                });
    }
}

