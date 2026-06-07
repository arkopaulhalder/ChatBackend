package com.ChatSystem.common_library.event;

import com.ChatSystem.common_library.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreatedEvent {

    private String messageId;
    private String conversationId;
    private String senderUuid;
    private String recipientUuid;
    private MessageType messageType;
    private String content;

    @Builder.Default
    private Instant sentAt = Instant.now();
}

