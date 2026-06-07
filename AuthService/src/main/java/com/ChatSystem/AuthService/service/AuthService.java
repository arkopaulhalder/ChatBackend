package com.ChatSystem.AuthService.service;

import com.ChatSystem.AuthService.dto.AuthResponse;
import com.ChatSystem.AuthService.dto.LoginRequest;
import com.ChatSystem.AuthService.dto.RegisterRequest;
import com.ChatSystem.AuthService.entity.AuthUser;
import com.ChatSystem.AuthService.entity.RefreshToken;
import com.ChatSystem.AuthService.entity.Role;
import com.ChatSystem.AuthService.entity.UserRole;
import com.ChatSystem.AuthService.repository.AuthUserRepository;
import com.ChatSystem.AuthService.repository.RoleRepository;
import com.ChatSystem.AuthService.security.CustomUserDetails;
import com.ChatSystem.common_library.event.UserCreatedEvent;
import com.ChatSystem.common_library.exception.BusinessException;
import com.ChatSystem.common_library.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRegistrationPublisher registrationPublisher;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check for conflicts before doing any work
        if (authUserRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already taken", HttpStatus.CONFLICT.value());
        }
        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered", HttpStatus.CONFLICT.value());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()
                && authUserRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessException("Phone number already registered", HttpStatus.CONFLICT.value());
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new NotFoundException("Role", "ROLE_USER"));

        AuthUser user = AuthUser.builder()
                .userUuid(UUID.randomUUID().toString())
                .username(request.getUsername())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        UserRole userRoleAssignment = UserRole.builder()
                .user(user)
                .role(userRole)
                .build();
        user.getUserRoles().add(userRoleAssignment);

        AuthUser savedUser = authUserRepository.save(user);

        // Publish async event so UserService creates the profile
        registrationPublisher.publish(UserCreatedEvent.builder()
                .userUuid(savedUser.getUserUuid())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .build());

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String accessToken = jwtService.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser, null, null, null);

        log.info("New user registered: uuid={} username={}", savedUser.getUserUuid(), savedUser.getUsername());

        return buildAuthResponse(savedUser, accessToken, refreshToken.getToken(), userDetails);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            // Do not reveal whether the identifier exists — generic message only
            throw new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED.value());
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        AuthUser user = userDetails.getAuthUser();

        authUserRepository.updateLastLoginAt(user.getId(), LocalDateTime.now());

        String accessToken = jwtService.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                user, request.getDeviceId(), request.getDeviceName(), ipAddress);

        log.info("User logged in: uuid={}", user.getUserUuid());

        return buildAuthResponse(user, accessToken, refreshToken.getToken(), userDetails);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshToken storedToken = refreshTokenService.validateAndGet(rawRefreshToken);
        AuthUser user = storedToken.getUser();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        // Rotate the refresh token — old one is revoked, new one issued
        refreshTokenService.revokeToken(rawRefreshToken);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(
                user, storedToken.getDeviceId(), storedToken.getDeviceName(), storedToken.getIpAddress());

        return buildAuthResponse(user, newAccessToken, newRefreshToken.getToken(), userDetails);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeToken(rawRefreshToken);
    }

    @Transactional(readOnly = true)
    public AuthUser getMe(String userUuid) {
        return authUserRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new NotFoundException("User", userUuid));
    }

    private AuthResponse buildAuthResponse(AuthUser user, String accessToken,
                                           String refreshToken, CustomUserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiryMs() / 1000)
                .userUuid(user.getUserUuid())
                .username(user.getUsername())
                .roles(roles)
                .build();
    }
}
