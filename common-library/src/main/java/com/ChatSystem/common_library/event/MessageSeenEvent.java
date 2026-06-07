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
public class MessageSeenEvent {

    private String messageId;
    private String conversationId;
    private String seenByUuid;

    @Builder.Default
    private Instant seenAt = Instant.now();
}
