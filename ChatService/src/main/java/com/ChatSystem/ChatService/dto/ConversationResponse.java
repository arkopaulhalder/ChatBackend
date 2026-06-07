package com.ChatSystem.ChatService.dto;

import com.ChatSystem.common_library.enums.ConversationType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ConversationResponse {
    private String conversationId;
    private ConversationType type;
    private List<String> participantIds;
    private String lastMessageText;
    private Instant lastMessageAt;
    private Instant createdAt;
}
