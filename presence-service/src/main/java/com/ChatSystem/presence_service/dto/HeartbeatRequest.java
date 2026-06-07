package com.ChatSystem.presence_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HeartbeatRequest {

    @NotBlank(message = "Session ID is required")
    private String sessionId;
}
