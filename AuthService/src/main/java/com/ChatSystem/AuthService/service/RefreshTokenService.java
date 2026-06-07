package com.ChatSystem.AuthService.service;

import com.ChatSystem.AuthService.config.JwtConfig;
import com.ChatSystem.AuthService.entity.AuthUser;
import com.ChatSystem.AuthService.entity.RefreshToken;
import com.ChatSystem.AuthService.repository.RefreshTokenRepository;
import com.ChatSystem.common_library.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;

    @Transactional
    public RefreshToken createRefreshToken(AuthUser user, String deviceId, String deviceName, String ipAddress) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .deviceId(deviceId)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .expiresAt(LocalDateTime.now().plusDays(jwtConfig.getRefreshTokenExpiryDays()))
                .build();

        return refreshTokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public RefreshToken validateAndGet(String rawToken) {
        RefreshToken token = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED.value()));

        if (!token.isValid()) {
            throw new BusinessException("Refresh token is expired or revoked", HttpStatus.UNAUTHORIZED.value());
        }

        return token;
    }

    @Transactional
    public void revokeToken(String rawToken) {
        refreshTokenRepository.findByToken(rawToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    // Runs at 2 AM every day — keeps refresh_tokens table clean
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now());
    }
}
