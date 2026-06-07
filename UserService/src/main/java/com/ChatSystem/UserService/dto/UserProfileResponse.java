package com.ChatSystem.UserService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {

    private String userUuid;
    private String username;
    private String displayName;
    private String bio;
    private String statusMessage;
    private String profilePhotoUrl;
    private boolean isPrivate;
    private LocalDateTime createdAt;
}
