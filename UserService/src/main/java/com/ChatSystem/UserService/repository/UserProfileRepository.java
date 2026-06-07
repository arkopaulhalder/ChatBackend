package com.ChatSystem.UserService.repository;


import com.ChatSystem.UserService.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserUuid(String userUuid);

    Optional<UserProfile> findByUsername(String username);

    boolean existsByUserUuid(String userUuid);

    // Case-insensitive search across username and displayName
    @Query("""
            SELECT u FROM UserProfile u
            WHERE u.isActive = true
            AND u.isPrivate = false
            AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
              OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<UserProfile> searchUsers(@Param("query") String query, Pageable pageable);

    void deleteByUserUuid(String requesterUuid);
}

