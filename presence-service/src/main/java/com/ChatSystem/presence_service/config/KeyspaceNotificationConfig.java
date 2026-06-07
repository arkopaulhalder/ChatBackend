package com.ChatSystem.presence_service.config;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Enables Redis keyspace notifications for key expiry events.
 *
 * Why this is needed:
 *   When a presence key expires (user stops sending heartbeats),
 *   Redis fires a "__keyevent@0__:expired" pub/sub message.
 *   Our RedisKeyExpirationListener catches this and fires a presence.updated event.
 *
 * The Redis config option "notify-keyspace-events" must include "Ex":
 *   E = keyevent events (fires on the key that expired)
 *   x = expired events
 *
 * We set this programmatically here so you don't need to edit redis.conf.
 * Note: this setting persists on the Redis server until restarted or changed.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class KeyspaceNotificationConfig {

    private final RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void enableKeyspaceNotifications() {
        try {
            redisTemplate.getConnectionFactory()
                    .getConnection()
                    .serverCommands()
                    .setConfig("notify-keyspace-events", "Ex");
            log.info("Redis keyspace expiry notifications enabled.");
        } catch (Exception e) {
            log.warn("Could not enable keyspace notifications — offline detection may not work: {}", e.getMessage());
        }
    }
}

