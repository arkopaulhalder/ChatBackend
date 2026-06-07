package com.ChatSystem.common_library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the parsed JWT claims forwarded by the API Gateway
 * as request headers to downstream services.
 *
 * Services should read X-User-UUID and X-User-Roles headers,
 * never re-parse the JWT token themselves.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtUserContext {

    private String userUuid;
    private String username;
    private List<String> roles;
}
