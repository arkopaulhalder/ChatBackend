package com.ChatSystem.UserService.controller;


import com.ChatSystem.common_library.constants.AppConstants;
import com.ChatSystem.common_library.dto.ApiResponse;
import com.ChatSystem.UserService.dto.AddContactRequest;
import com.ChatSystem.UserService.dto.ContactResponse;
import com.ChatSystem.UserService.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userUuid}/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContactResponse>>> getContacts(
            @PathVariable String userUuid,
            @RequestHeader(AppConstants.USER_UUID_HEADER) String requesterUuid) {

        // Only fetch your own contacts
        return ResponseEntity.ok(ApiResponse.ok(
                contactService.getContacts(requesterUuid)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContactResponse>> addContact(
            @PathVariable String userUuid,
            @RequestHeader(AppConstants.USER_UUID_HEADER) String requesterUuid,
            @Valid @RequestBody AddContactRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Contact added",
                        contactService.addContact(requesterUuid, request)));
    }

    @DeleteMapping("/{contactUserUuid}")
    public ResponseEntity<ApiResponse<Void>> removeContact(
            @PathVariable String userUuid,
            @PathVariable String contactUserUuid,
            @RequestHeader(AppConstants.USER_UUID_HEADER) String requesterUuid) {

        contactService.removeContact(requesterUuid, contactUserUuid);
        return ResponseEntity.ok(ApiResponse.ok("Contact removed", null));
    }
}

