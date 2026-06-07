package com.ChatSystem.ChatService.dto;

import com.ChatSystem.common_library.enums.MessageStatus;
import com.ChatSystem.common_library.enums.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MessageResponse {
    private String messageId;
    private String conversationId;
    private String senderId;
    private MessageType messageType;
    private String content;
    private MessageStatus status;
    private String replyToMessageId;
    private Instant sentAt;
    private Instant deliveredAt;
    private Instant seenAt;
    private boolean edited;
    private boolean deleted;
}
