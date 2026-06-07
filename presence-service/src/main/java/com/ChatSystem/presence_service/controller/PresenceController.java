package com.ChatSystem.presence_service.controller;


import com.ChatSystem.common_library.constants.AppConstants;
import com.ChatSystem.common_library.dto.ApiResponse;
import com.ChatSystem.presence_service.dto.HeartbeatRequest;
import com.ChatSystem.presence_service.dto.PresenceResponse;
import com.ChatSystem.presence_service.service.HeartbeatService;
import com.ChatSystem.presence_service.service.PresenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;
    private final HeartbeatService heartbeatService;

    /**
     * Called when the client connects (opens the app / establishes WebSocket).
     * Marks the user as ONLINE and starts their presence TTL.
     */
    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<Void>> connect(
            @RequestHeader(AppConstants.USER_UUID_HEADER) String userUuid,
            @Valid @RequestBody HeartbeatRequest request) {

        presenceService.markOnline(userUuid, request.getSessionId());
        return ResponseEntity.ok(ApiResponse.ok("Connected", null));
    }

    /**
     * Called periodically by the client (~every 30s) to keep the presence key alive.
     * Must be called more frequently than presence.ttl-seconds (default 60s).
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<ApiResponse<Void>> heartbeat(
            @RequestHeader(AppConstants.USER_UUID_HEADER) String userUuid,
            @Valid @RequestBody HeartbeatRequest request) {

        heartbeatService.processHeartbeat(userUuid, request.getSessionId());
        return ResponseEntity.ok(ApiResponse.ok("Heartbeat received", null));
    }

    /**
     * Called when the client cleanly disconnects (closes the app).
     * Marks offline immediately instead of waiting for TTL expiry.
     */
    @PostMapping("/disconnect")
    public ResponseEntity<ApiResponse<Void>> disconnect(
            @RequestHeader(AppConstants.USER_UUID_HEADER) String userUuid) {

        presenceService.markOffline(userUuid);
        return ResponseEntity.ok(ApiResponse.ok("Disconnected", null));
    }

    /**
     * Get presence status for a single user.
     */
    @GetMapping("/{userUuid}")
    public ResponseEntity<ApiResponse<PresenceResponse>> getPresence(
            @PathVariable String userUuid) {

        return ResponseEntity.ok(ApiResponse.ok(presenceService.getPresence(userUuid)));
    }

    /**
     * Bulk presence check — client sends a list of userUuids to check.
     * Used when loading a contact list to show who is online.
     */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<PresenceResponse>>> getBulkPresence(
            @RequestBody List<String> userUuids) {

        if (userUuids == null || userUuids.isEmpty() || userUuids.size() > 100) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Provide between 1 and 100 user UUIDs"));
        }

        return ResponseEntity.ok(ApiResponse.ok(presenceService.getBulkPresence(userUuids)));
    }
}

