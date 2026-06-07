package com.ChatSystem.UserService.service;


import com.ChatSystem.common_library.exception.BusinessException;
import com.ChatSystem.common_library.exception.NotFoundException;
import com.ChatSystem.UserService.dto.AddContactRequest;
import com.ChatSystem.UserService.dto.ContactResponse;
import com.ChatSystem.UserService.entity.Contact;
import com.ChatSystem.UserService.mapper.UserMapper;
import com.ChatSystem.UserService.repository.ContactRepository;
import com.ChatSystem.UserService.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<ContactResponse> getContacts(String ownerUuid) {
        return contactRepository
                .findByOwnerUserUuidOrderByIsFavoriteDescCreatedAtDesc(ownerUuid)
                .stream()
                .map(contact -> {
                    var contactProfile = userProfileRepository
                            .findByUserUuid(contact.getContactUserUuid())
                            .orElse(null);
                    return userMapper.toContactResponse(contact, contactProfile);
                })
                .toList();
    }

    @Transactional
    public ContactResponse addContact(String ownerUuid, AddContactRequest request) {
        if (ownerUuid.equals(request.getContactUserUuid())) {
            throw new BusinessException("Cannot add yourself as a contact", HttpStatus.BAD_REQUEST.value());
        }

        // Verify the target user actually exists
        var contactProfile = userProfileRepository
                .findByUserUuid(request.getContactUserUuid())
                .orElseThrow(() -> new NotFoundException("User", request.getContactUserUuid()));

        if (contactRepository.existsByOwnerUserUuidAndContactUserUuid(
                ownerUuid, request.getContactUserUuid())) {
            throw new BusinessException("Contact already exists", HttpStatus.CONFLICT.value());
        }

        Contact contact = Contact.builder()
                .ownerUserUuid(ownerUuid)
                .contactUserUuid(request.getContactUserUuid())
                .contactName(request.getContactName())
                .build();

        Contact saved = contactRepository.save(contact);
        log.info("Contact added: owner={} contact={}", ownerUuid, request.getContactUserUuid());
        return userMapper.toContactResponse(saved, contactProfile);
    }

    @Transactional
    public void removeContact(String ownerUuid, String contactUserUuid) {
        if (!contactRepository.existsByOwnerUserUuidAndContactUserUuid(ownerUuid, contactUserUuid)) {
            throw new NotFoundException("Contact", contactUserUuid);
        }
        contactRepository.deleteByOwnerUserUuidAndContactUserUuid(ownerUuid, contactUserUuid);
        log.info("Contact removed: owner={} contact={}", ownerUuid, contactUserUuid);
    }
}

