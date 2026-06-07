package com.ChatSystem.AuthService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    // Accepts username or email — resolved in AuthService
    @NotBlank(message = "identifier is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;

    private String deviceId;    // optional
    private String deviceName;  // optional
}
