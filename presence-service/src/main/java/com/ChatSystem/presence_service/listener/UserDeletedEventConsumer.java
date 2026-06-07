package com.ChatSystem.presence_service.listener;

import com.ChatSystem.common_library.constants.KafkaTopics;
import com.ChatSystem.common_library.event.UserDeletedEvent;
import com.ChatSystem.presence_service.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletedEventConsumer {

    private final PresenceService presenceService;

    @KafkaListener(
            topics = KafkaTopics.USER_DELETED,
            groupId = "presence-service-delete-group"
    )
    public void consume(UserDeletedEvent event) {
        log.info("Processing presence deletion for uuid={}", event.getUserUuid());
        presenceService.deleteUserPresence(event.getUserUuid());
    }
}
