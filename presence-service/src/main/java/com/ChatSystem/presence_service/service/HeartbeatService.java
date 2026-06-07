package com.ChatSystem.presence_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HeartbeatService {

    private final PresenceService presenceService;

    public void processHeartbeat(String userUuid, String sessionId) {
        presenceService.heartbeat(userUuid, sessionId);
    }
}
