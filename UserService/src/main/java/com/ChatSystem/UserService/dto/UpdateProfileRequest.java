package com.ChatSystem.UserService.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 1, max = 100, message = "Display name must be 1-100 characters")
    private String displayName;

    @Size(max = 500, message = "Bio must be under 500 characters")
    private String bio;

    @Size(max = 150, message = "Status message must be under 150 characters")
    private String statusMessage;

    @Size(max = 500, message = "Profile photo URL too long")
    private String profilePhotoUrl;

    private Boolean isPrivate;
}
