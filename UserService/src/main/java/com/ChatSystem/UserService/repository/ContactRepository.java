package com.ChatSystem.UserService.repository;

import com.ChatSystem.UserService.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByOwnerUserUuidOrderByIsFavoriteDescCreatedAtDesc(String ownerUserUuid);

    Optional<Contact> findByOwnerUserUuidAndContactUserUuid(String ownerUserUuid, String contactUserUuid);

    boolean existsByOwnerUserUuidAndContactUserUuid(String ownerUserUuid, String contactUserUuid);

    void deleteByOwnerUserUuidAndContactUserUuid(String ownerUserUuid, String contactUserUuid);

    void deleteAllByOwnerUserUuid(String ownerUserUuid);

    void deleteAllByContactUserUuid(String contactUserUuid);
}
