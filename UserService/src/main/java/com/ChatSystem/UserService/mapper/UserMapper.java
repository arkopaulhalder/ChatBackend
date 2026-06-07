package com.ChatSystem.UserService.mapper;


import com.ChatSystem.UserService.dto.ContactResponse;
import com.ChatSystem.UserService.dto.UserProfileResponse;
import com.ChatSystem.UserService.entity.Contact;
import com.ChatSystem.UserService.entity.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfileResponse toProfileResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .userUuid(profile.getUserUuid())
                .username(profile.getUsername())
                .displayName(profile.getDisplayName())
                .bio(profile.getBio())
                .statusMessage(profile.getStatusMessage())
                .profilePhotoUrl(profile.getProfilePhotoUrl())
                .isPrivate(profile.isPrivate())
                .createdAt(profile.getCreatedAt())
                .build();
    }

    public ContactResponse toContactResponse(Contact contact, UserProfile contactProfile) {
        return ContactResponse.builder()
                .contactUserUuid(contact.getContactUserUuid())
                .contactName(contact.getContactName())
                .username(contactProfile != null ? contactProfile.getUsername() : null)
                .displayName(contactProfile != null ? contactProfile.getDisplayName() : null)
                .profilePhotoUrl(contactProfile != null ? contactProfile.getProfilePhotoUrl() : null)
                .isFavorite(contact.isFavorite())
                .addedAt(contact.getCreatedAt())
                .build();
    }
}

