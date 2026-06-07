package com.ChatSystem.presence_service.service;


import com.ChatSystem.common_library.enums.UserStatus;
import com.ChatSystem.presence_service.dto.PresenceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Manages presence state in Redis.
 *
 * Key schema:
 *   presence:user:{uuid}      → "ONLINE"  TTL 60s  (refreshed by heartbeat)
 *   presence:lastseen:{uuid}  → ISO timestamp       (set on disconnect/expiry)
 *   presence:session:{sid}    → userUuid            (maps session ID to user)
 *
 * How it works:
 *   - Client calls POST /presence/heartbeat every ~30s
 *   - Heartbeat refreshes the TTL on presence:user:{uuid}
 *   - If TTL expires (client died without calling disconnect), Redis fires
 *     a keyspace expiry event, caught by RedisKeyExpirationListener
 *   - Listener updates lastseen and publishes presence.updated OFFLINE to Kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService {

    private static final String PRESENCE_KEY_PREFIX  = "presence:user:";
    private static final String LASTSEEN_KEY_PREFIX  = "presence:lastseen:";
    private static final String SESSION_KEY_PREFIX   = "presence:session:";

    private final RedisTemplate<String, String> redisTemplate;
    private final PresenceEventPublisher eventPublisher;

    @Value("${presence.ttl-seconds:60}")
    private long ttlSeconds;

    public void markOnline(String userUuid, String sessionId) {
        String presenceKey = PRESENCE_KEY_PREFIX + userUuid;
        String sessionKey  = SESSION_KEY_PREFIX + sessionId;

        redisTemplate.opsForValue().set(presenceKey, UserStatus.ONLINE.name(), ttlSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(sessionKey, userUuid, ttlSeconds * 2, TimeUnit.SECONDS);
        redisTemplate.delete(LASTSEEN_KEY_PREFIX + userUuid); // clear stale last-seen

        eventPublisher.publishPresenceUpdate(userUuid, UserStatus.ONLINE);
        log.debug("User marked online: userUuid={}", userUuid);
    }

    /**
     * Refreshes the TTL — called by the heartbeat endpoint every ~30 seconds.
     * Does not publish a Kafka event (already online, no state change).
     */
    public void heartbeat(String userUuid, String sessionId) {
        String presenceKey = PRESENCE_KEY_PREFIX + userUuid;
        Boolean exists = redisTemplate.hasKey(presenceKey);

        if (Boolean.FALSE.equals(exists)) {
            // Key expired between heartbeats — treat as reconnect
            markOnline(userUuid, sessionId);
            return;
        }

        redisTemplate.expire(presenceKey, ttlSeconds, TimeUnit.SECONDS);
        log.debug("Heartbeat received: userUuid={}", userUuid);
    }

    public void markOffline(String userUuid) {
        redisTemplate.delete(PRESENCE_KEY_PREFIX + userUuid);
        redisTemplate.opsForValue().set(
                LASTSEEN_KEY_PREFIX + userUuid,
                Instant.now().toString()
        );
        eventPublisher.publishPresenceUpdate(userUuid, UserStatus.OFFLINE);
        log.debug("User marked offline: userUuid={}", userUuid);
    }

    public PresenceResponse getPresence(String userUuid) {
        boolean isOnline = Boolean.TRUE.equals(
                redisTemplate.hasKey(PRESENCE_KEY_PREFIX + userUuid));

        if (isOnline) {
            return PresenceResponse.builder()
                    .userUuid(userUuid)
                    .status(UserStatus.ONLINE)
                    .build();
        }

        String lastSeenStr = redisTemplate.opsForValue().get(LASTSEEN_KEY_PREFIX + userUuid);
        Instant lastSeen = lastSeenStr != null ? Instant.parse(lastSeenStr) : null;

        return PresenceResponse.builder()
                .userUuid(userUuid)
                .status(UserStatus.OFFLINE)
                .lastSeenAt(lastSeen)
                .build();
    }

    public List<PresenceResponse> getBulkPresence(List<String> userUuids) {
        return userUuids.stream()
                .map(this::getPresence)
                .toList();
    }

    /**
     * Called by RedisKeyExpirationListener when presence:user:{uuid} key expires.
     * Extracts the userUuid from the expired key name and marks them offline.
     */
    public void handleKeyExpiry(String expiredKey) {
        if (!expiredKey.startsWith(PRESENCE_KEY_PREFIX)) return;

        String userUuid = expiredKey.substring(PRESENCE_KEY_PREFIX.length());
        log.info("Presence key expired for userUuid={} — marking offline", userUuid);

        redisTemplate.opsForValue().set(
                LASTSEEN_KEY_PREFIX + userUuid,
                Instant.now().toString()
        );
        eventPublisher.publishPresenceUpdate(userUuid, UserStatus.OFFLINE);
    }

    //  called by Kafka consumer for deleting user data on account deletion
    public void deleteUserPresence(String userUuid) {
        redisTemplate.delete("presence:user:" + userUuid);
        redisTemplate.delete("presence:lastseen:" + userUuid);
        log.info("Presence data deleted for userUuid={}", userUuid);
    }
}

