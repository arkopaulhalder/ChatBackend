package com.ChatSystem.UserService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ContactResponse {

    private String contactUserUuid;
    private String contactName;       // custom name the owner gave this contact
    private String username;          // from the contact's profile
    private String displayName;       // from the contact's profile
    private String profilePhotoUrl;
    private boolean isFavorite;
    private LocalDateTime addedAt;
}
