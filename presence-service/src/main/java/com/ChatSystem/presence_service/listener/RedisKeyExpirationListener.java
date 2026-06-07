package com.ChatSystem.presence_service.listener;


import com.ChatSystem.presence_service.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Listens for Redis keyspace expiry notifications.
 *
 * When a client stops heartbeating and the presence:user:{uuid} key expires,
 * this listener fires and triggers the offline transition.
 *
 * This covers the crash/disconnect case where the client never calls
 * POST /presence/disconnect explicitly.
 *
 * Prerequisites:
 *   - Redis server must have keyspace notifications enabled.
 *     KeyspaceNotificationConfig does this programmatically on startup.
 *   - The RedisMessageListenerContainer bean must be configured (RedisConfig).
 */
@Slf4j
@Component
//@RequiredArgsConstructor
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final PresenceService presenceService;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
                                      PresenceService presenceService) {
        super(listenerContainer);
        this.presenceService = presenceService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.debug("Redis key expired: {}", expiredKey);
        // PresenceService filters to only handle presence:user:* keys
        presenceService.handleKeyExpiry(expiredKey);
    }
}

