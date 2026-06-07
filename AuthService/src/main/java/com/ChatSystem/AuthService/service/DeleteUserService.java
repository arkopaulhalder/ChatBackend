package com.ChatSystem.AuthService.service;

import com.ChatSystem.AuthService.repository.AuthUserRepository;
import com.ChatSystem.AuthService.repository.RefreshTokenRepository;
import com.ChatSystem.common_library.constants.KafkaTopics;
import com.ChatSystem.common_library.event.UserDeletedEvent;
import com.ChatSystem.common_library.exception.NotFoundException;
import com.ChatSystem.AuthService.entity.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteUserService {

    private final AuthUserRepository authUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void deleteByUuid(String userUuid) {
        AuthUser user = authUserRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new NotFoundException("User", userUuid));
        deleteUser(user);
    }

    @Transactional
    public void deleteByUsername(String username) {
        AuthUser user = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User", username));
        deleteUser(user);
    }

    private void deleteUser(AuthUser user) {
        String userUuid = user.getUserUuid();
        String username = user.getUsername();

        // 1. Revoke all refresh tokens first
        refreshTokenRepository.revokeAllByUserId(user.getId());

        // 2. Delete auth user — cascades to user_roles and refresh_tokens via FK
        authUserRepository.delete(user);

        // 3. Publish event — UserService and ChatService consume and delete their data
        kafkaTemplate.send(KafkaTopics.USER_DELETED, userUuid,
                UserDeletedEvent.builder()
                        .userUuid(userUuid)
                        .username(username)
                        .build());

        log.info("User deleted: uuid={} username={}", userUuid, username);
    }
}
