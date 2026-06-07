package com.ChatSystem.AuthService.controller;

import com.ChatSystem.AuthService.service.DeleteUserService;
import com.ChatSystem.common_library.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DeleteUserService deleteUserService;

    // Delete by UUID
    @DeleteMapping("/users/{userUuid}")
    public ResponseEntity<ApiResponse<Void>> deleteByUuid(
            @PathVariable String userUuid) {

        deleteUserService.deleteByUuid(userUuid);
        return ResponseEntity.ok(
                ApiResponse.ok("User and all associated data deleted", null));
    }

    // Delete by username
    @DeleteMapping("/users/username/{username}")
    public ResponseEntity<ApiResponse<Void>> deleteByUsername(
            @PathVariable String username) {

        deleteUserService.deleteByUsername(username);
        return ResponseEntity.ok(
                ApiResponse.ok("User and all associated data deleted", null));
    }
}
