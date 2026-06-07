package com.ChatSystem.common_library.event;

import com.ChatSystem.common_library.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceUpdatedEvent {

    private String userUuid;
    private UserStatus status;

    @Builder.Default
    private Instant occurredAt = Instant.now();
}
