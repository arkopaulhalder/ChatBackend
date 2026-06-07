package com.ChatSystem.AuthService.controller;

import com.ChatSystem.AuthService.dto.*;
import com.ChatSystem.AuthService.entity.AuthUser;
import com.ChatSystem.AuthService.mapper.AuthMapper;
import com.ChatSystem.AuthService.service.AuthService;
import com.ChatSystem.common_library.constants.AppConstants;
import com.ChatSystem.common_library.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, token refresh and logout")
public class AuthController {

    private final AuthService authService;
    private final AuthMapper authMapper;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = extractClientIp(httpRequest);
        AuthResponse response = authService.login(request, ipAddress);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke refresh token (logout)")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request) {

        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user identity from JWT header")
    public ResponseEntity<ApiResponse<AuthResponse>> me(
            @RequestHeader(AppConstants.USER_UUID_HEADER) String userUuid,
            @RequestHeader(AppConstants.USER_ROLES_HEADER) String rolesHeader) {

        AuthUser user = authService.getMe(userUuid);
        // Roles come from the gateway header — trust the gateway, don't re-query
        var roles = java.util.Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(authMapper.toMeResponse(user, roles)));
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

