package com.ChatSystem.AuthService.mapper;

import com.ChatSystem.AuthService.dto.AuthResponse;
import com.ChatSystem.AuthService.entity.AuthUser;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Thin mapper layer. If the project later adds MapStruct, replace these
 * manual methods. Kept simple for MVP — no external mapper dependency.
 */
@Component
public class AuthMapper {

    public AuthResponse toMeResponse(AuthUser user, List<String> roles) {
        return AuthResponse.builder()
                .userUuid(user.getUserUuid())
                .username(user.getUsername())
                .roles(roles)
                // No tokens in /me — caller only needs identity
                .build();
    }
}
