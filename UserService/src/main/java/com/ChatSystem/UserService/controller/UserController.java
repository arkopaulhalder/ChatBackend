package com.ChatSystem.UserService.controller;

import com.ChatSystem.UserService.entity.UserProfile;
import com.ChatSystem.UserService.repository.UserProfileRepository;
import com.ChatSystem.common_library.constants.AppConstants;
import com.ChatSystem.common_library.dto.ApiResponse;
import com.ChatSystem.common_library.exception.BusinessException;
import com.ChatSystem.UserService.dto.SearchUsersResponse;
import com.ChatSystem.UserService.dto.UpdateProfileRequest;
import com.ChatSystem.UserService.dto.UserProfileResponse;
import com.ChatSystem.UserService.service.UserProfileService;
import com.ChatSystem.common_library.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;
    private final UserProfileRepository userProfileRepository;

    @GetMapping("/{userUuid}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @PathVariable String userUuid) {

        return ResponseEntity.ok(ApiResponse.ok(userProfileService.getProfile(userUuid)));
    }

    @GetMapping("/{userUuid}/username")
    public ResponseEntity<ApiResponse<Map<String, String>>> getUsername(
            @PathVariable String userUuid) {

        UserProfile profile = userProfileRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new NotFoundException("User", userUuid));

        return ResponseEntity.ok(ApiResponse.ok(
                Map.of(
                        "userUuid",     profile.getUserUuid(),
                        "username",     profile.getUsername(),
                        "displayName",  profile.getDisplayName()
                )
        ));
    }

    @PutMapping("/{userUuid}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @PathVariable String userUuid,
            @RequestHeader(AppConstants.USER_UUID_HEADER) String requesterUuid,
            @Valid @RequestBody UpdateProfileRequest request) {

        // Users can only update their own profile
        if (!userUuid.equals(requesterUuid)) {
            throw new BusinessException("You can only update your own profile", HttpStatus.FORBIDDEN.value());
        }

        return ResponseEntity.ok(ApiResponse.ok(
                userProfileService.updateProfile(userUuid, request)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchUsersResponse>> searchUsers(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.ok(
                userProfileService.searchUsers(query, page, size)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @RequestHeader(AppConstants.USER_UUID_HEADER) String requesterUuid) {

        return ResponseEntity.ok(ApiResponse.ok(
                userProfileService.getProfile(requesterUuid)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteMyProfile(
            @RequestHeader(AppConstants.USER_UUID_HEADER) String requesterUuid) {

        userProfileService.deleteProfile(requesterUuid);
        return ResponseEntity.ok(ApiResponse.ok("Profile deleted", null));
    }
}

