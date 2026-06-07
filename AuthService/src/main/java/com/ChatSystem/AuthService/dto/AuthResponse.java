package com.ChatSystem.AuthService.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;       // access token lifetime in seconds
    private String userUuid;
    private String username;
    private List<String> roles;
}
