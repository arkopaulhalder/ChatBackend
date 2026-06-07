package com.ChatSystem.UserService.service;

import com.ChatSystem.common_library.constants.KafkaTopics;
import com.ChatSystem.common_library.event.UserDeletedEvent;
import com.ChatSystem.UserService.repository.ContactRepository;
import com.ChatSystem.UserService.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletedEventConsumer {

    private final UserProfileRepository userProfileRepository;
    private final ContactRepository contactRepository;

    @KafkaListener(
            topics = KafkaTopics.USER_DELETED,
            groupId = "user-service-delete-group",
            containerFactory = "userDeletedKafkaListenerContainerFactory"
    )
    // No @Transactional here
    public void consume(UserDeletedEvent event) {
        String uuid = event.getUserUuid();
        log.info("Processing user deletion for uuid={}", uuid);

        try {
            deleteUserData(uuid);
            log.info("UserService data deleted for uuid={}", uuid);
        } catch (Exception e) {
            log.error("Failed to delete UserService data for uuid={}: {}", uuid, e.getMessage(), e);
        }
    }

    @Transactional  // transaction lives here, called from the listener
    public void deleteUserData(String uuid) {
        userProfileRepository.findByUserUuid(uuid)
                .ifPresent(userProfileRepository::delete);

        contactRepository.deleteAllByOwnerUserUuid(uuid);
        contactRepository.deleteAllByContactUserUuid(uuid);
    }
}