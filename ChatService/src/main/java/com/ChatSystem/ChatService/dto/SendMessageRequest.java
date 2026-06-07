package com.ChatSystem.ChatService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank(message = "Conversation ID is required")
    private String conversationId;

    @NotBlank(message = "Content cannot be empty")
    @Size(max = 4000, message = "Message too long")
    private String content;

    private String replyToMessageId;   // optional
}