package com.ChatSystem.AuthService.repository;

import com.ChatSystem.AuthService.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByUsername(String username);

    Optional<AuthUser> findByEmail(String email);

    Optional<AuthUser> findByUserUuid(String userUuid);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Modifying
    @Query("UPDATE AuthUser u SET u.lastLoginAt = :loginAt WHERE u.id = :id")
    void updateLastLoginAt(@Param("id") Long id, @Param("loginAt") LocalDateTime loginAt);
}
