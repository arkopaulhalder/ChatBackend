package com.ChatSystem.presence_service.dto;

import com.ChatSystem.common_library.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PresenceResponse {

    private String userUuid;
    private UserStatus status;
    private Instant lastSeenAt;    // null if currently online
}
