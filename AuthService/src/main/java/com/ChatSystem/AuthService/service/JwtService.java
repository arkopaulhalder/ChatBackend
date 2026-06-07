package com.ChatSystem.AuthService.service;

import com.ChatSystem.AuthService.config.JwtConfig;
import com.ChatSystem.AuthService.security.CustomUserDetails;
import com.ChatSystem.common_library.util.JwtUtils;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;

    public String generateAccessToken(CustomUserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(userDetails.getAuthUser().getUserUuid())
                .claim("username", userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtConfig.getAccessTokenExpiryMs()))
                .signWith(JwtUtils.buildKey(jwtConfig.getSecret()))
                .compact();
    }

    public boolean isTokenValid(String token) {
        return JwtUtils.isTokenValid(token, jwtConfig.getSecret());
    }

    public String extractUserUuid(String token) {
        return JwtUtils.extractUserUuid(token, jwtConfig.getSecret());
    }

    public long getAccessTokenExpiryMs() {
        return jwtConfig.getAccessTokenExpiryMs();
    }
}

