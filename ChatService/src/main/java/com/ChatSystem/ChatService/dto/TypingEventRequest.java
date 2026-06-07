package com.ChatSystem.ChatService.dto;

import lombok.Data;

@Data
public class TypingEventRequest {
    private String conversationId;
    private boolean typing;    // true = started typing, false = stopped
}
