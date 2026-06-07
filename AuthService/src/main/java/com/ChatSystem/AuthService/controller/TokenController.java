package com.ChatSystem.AuthService.controller;

import com.ChatSystem.AuthService.dto.AuthResponse;
import com.ChatSystem.AuthService.dto.RefreshTokenRequest;
import com.ChatSystem.AuthService.service.AuthService;
import com.ChatSystem.common_library.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Token Management", description = "Refresh access token")
public class TokenController {

    private final AuthService authService;

    @PostMapping("/refresh")
    @Operation(summary = "Issue a new access token using a valid refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed", response));
    }
}

