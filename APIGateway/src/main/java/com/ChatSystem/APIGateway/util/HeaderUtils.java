package com.ChatSystem.APIGateway.util;


import com.ChatSystem.common_library.constants.AppConstants;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Arrays;
import java.util.List;

/**
 * Used by the gateway internally to build and read identity headers.
 *
 * Downstream services (ChatService, UserService) have their own equivalent
 * in their respective utility classes — they read the same header names
 * defined in AppConstants.
 */
public final class HeaderUtils {

    private HeaderUtils() {}

    public static String getUserUuid(ServerHttpRequest request) {
        return request.getHeaders().getFirst(AppConstants.USER_UUID_HEADER);
    }

    public static List<String> getUserRoles(ServerHttpRequest request) {
        String rolesHeader = request.getHeaders().getFirst(AppConstants.USER_ROLES_HEADER);
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return List.of();
        }
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public static String getCorrelationId(ServerHttpRequest request) {
        return request.getHeaders().getFirst(AppConstants.CORRELATION_ID_HEADER);
    }
}

