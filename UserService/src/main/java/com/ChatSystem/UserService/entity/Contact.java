package com.ChatSystem.UserService.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "contacts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"owner_user_uuid", "contact_user_uuid"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // No FK — cross-service reference. UserService owns profiles but
    // contacts just store UUIDs; there is no foreign key to user_profiles.
    @Column(name = "owner_user_uuid", nullable = false, length = 36)
    private String ownerUserUuid;

    @Column(name = "contact_user_uuid", nullable = false, length = 36)
    private String contactUserUuid;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private boolean isFavorite = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

