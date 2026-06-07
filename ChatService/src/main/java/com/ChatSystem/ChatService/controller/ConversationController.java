package com.ChatSystem.ChatService.controller;


import com.ChatSystem.ChatService.dto.ConversationResponse;
import com.ChatSystem.ChatService.dto.CreatePrivateConversationRequest;
import com.ChatSystem.ChatService.service.ConversationService;
import com.ChatSystem.common_library.constants.AppConstants;
import com.ChatSystem.common_library.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/private")
    public ResponseEntity<ApiResponse<ConversationResponse>> createPrivate(
            @Valid @RequestBody CreatePrivateConversationRequest request,
            @RequestHeader(AppConstants.USER_UUID_HEADER) String requesterUuid) {

        ConversationResponse response = conversationService
                .getOrCreatePrivateConversation(requesterUuid, request.getTargetUserUuid());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getMyConversations(
            @RequestHeader(AppConstants.USER_UUID_HEADER) String requesterUuid) {

        return ResponseEntity.ok(ApiResponse.ok(
                conversationService.getUserConversations(requesterUuid)));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
            @PathVariable String conversationId,
            @RequestHeader(AppConstants.USER_UUID_HEADER) String requesterUuid) {

        return ResponseEntity.ok(ApiResponse.ok(
                conversationService.getConversation(conversationId, requesterUuid)));
    }
}

