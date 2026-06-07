package com.ChatSystem.common_library.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {

    private String userUuid;
    private String username;
    private String email;
    private String phoneNumber; // nullable

    @Builder.Default
    private Instant occurredAt = Instant.now();
}
