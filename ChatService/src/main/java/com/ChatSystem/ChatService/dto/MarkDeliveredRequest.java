package com.ChatSystem.ChatService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MarkDeliveredRequest {

    @NotBlank
    private String messageId;

    @NotBlank
    private String conversationId;
}
