package com.ChatSystem.ChatService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePrivateConversationRequest {

    @NotBlank(message = "Target user UUID is required")
    private String targetUserUuid;
}
