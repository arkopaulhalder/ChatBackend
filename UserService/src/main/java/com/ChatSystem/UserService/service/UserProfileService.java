package com.ChatSystem.UserService.service;

import com.ChatSystem.common_library.event.UserCreatedEvent;
import com.ChatSystem.common_library.exception.BusinessException;
import com.ChatSystem.common_library.exception.NotFoundException;
import com.ChatSystem.UserService.dto.SearchUsersResponse;
import com.ChatSystem.UserService.dto.UpdateProfileRequest;
import com.ChatSystem.UserService.dto.UserProfileResponse;
import com.ChatSystem.UserService.entity.UserProfile;
import com.ChatSystem.UserService.mapper.UserMapper;
import com.ChatSystem.UserService.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;

    /**
     * Called by the Kafka consumer when AuthService publishes user.created.
     * Idempotent — if the profile already exists, this is a no-op.
     */
    @Transactional
    public void createProfileFromEvent(UserCreatedEvent event) {

        // 1. Check by userUuid — handles duplicate Kafka event delivery
        if (userProfileRepository.existsByUserUuid(event.getUserUuid())) {
            log.warn("Profile already exists for userUuid={}, skipping creation", event.getUserUuid());
            return;
        }

        // 2. Check by username — handles re-registration with same username after deletion
        if (userProfileRepository.findByUsername(event.getUsername()).isPresent()) {
            log.warn("Profile already exists for username={}, skipping", event.getUsername());
            return;
        }

        UserProfile profile = UserProfile.builder()
                .userUuid(event.getUserUuid())
                .username(event.getUsername())
                .displayName(event.getUsername()) // default display name = username, user can update later
                .email(event.getEmail())
                .phoneNumber(event.getPhoneNumber())
                .build();

        userProfileRepository.save(profile);
        log.info("Profile created for userUuid={}", event.getUserUuid());
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String userUuid) {
        UserProfile profile = userProfileRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new NotFoundException("UserProfile", userUuid));
        return userMapper.toProfileResponse(profile);
    }

    @Transactional
    public UserProfileResponse updateProfile(String userUuid, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new NotFoundException("UserProfile", userUuid));

        if (request.getDisplayName() != null) profile.setDisplayName(request.getDisplayName());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getStatusMessage() != null) profile.setStatusMessage(request.getStatusMessage());
        if (request.getProfilePhotoUrl() != null) profile.setProfilePhotoUrl(request.getProfilePhotoUrl());
        if (request.getIsPrivate() != null) profile.setPrivate(request.getIsPrivate());

        return userMapper.toProfileResponse(userProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public SearchUsersResponse searchUsers(String query, int page, int size) {
        if (query == null || query.trim().length() < 2) {
            throw new BusinessException("Search query must be at least 2 characters", HttpStatus.BAD_REQUEST.value());
        }

        Page<UserProfile> results = userProfileRepository.searchUsers(
                query.trim(), PageRequest.of(page, size));

        return SearchUsersResponse.builder()
                .users(results.getContent().stream()
                        .map(userMapper::toProfileResponse)
                        .toList())
                .totalPages(results.getTotalPages())
                .totalElements(results.getTotalElements())
                .currentPage(results.getNumber())
                .build();
    }

    public void deleteProfile(String requesterUuid) {
        if (!userProfileRepository.existsByUserUuid(requesterUuid)) {
            throw new NotFoundException("UserProfile", requesterUuid);
        }
        userProfileRepository.deleteByUserUuid(requesterUuid);
        log.info("Profile deleted for userUuid={}", requesterUuid);
    }
}

