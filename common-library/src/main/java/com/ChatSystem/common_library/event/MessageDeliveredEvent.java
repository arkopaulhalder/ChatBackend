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
public class MessageDeliveredEvent {

    private String messageId;
    private String conversationId;
    private String recipientUuid;

    @Builder.Default
    private Instant deliveredAt = Instant.now();
}
