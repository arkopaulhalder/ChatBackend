package com.ChatSystem.common_library.constants;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String[] PUBLIC_ENDPOINTS = {
            "/auth/register",
            "/auth/login",
            "/auth/refresh",
            "/actuator/health"
    };
}
