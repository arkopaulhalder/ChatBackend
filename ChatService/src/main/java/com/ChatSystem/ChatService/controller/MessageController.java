package com.ChatSystem.ChatService.controller;


import com.ChatSystem.ChatService.dto.MarkDeliveredRequest;
import com.ChatSystem.ChatService.dto.MarkSeenRequest;
import com.ChatSystem.ChatService.dto.MessageResponse;
import com.ChatSystem.ChatService.dto.SendMessageRequest;
import com.ChatSystem.ChatService.service.DeliveryService;
import com.ChatSystem.ChatService.service.MessageService;
import com.ChatSystem.ChatService.service.SeenService;
import com.ChatSystem.common_library.constants.AppConstants;
import com.ChatSystem.common_library.dto.ApiResponse;
import com.ChatSystem.common_library.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final DeliveryService deliveryService;
    private final SeenService seenService;

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            @RequestHeader(AppConstants.USER_UUID_HEADER) String senderUuid) {

        return ResponseEntity.ok(ApiResponse.ok(
                messageService.sendMessage(request, senderUuid)));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<PageResponse<MessageResponse>>> getMessages(
            @PathVariable String conversationId,
            @RequestHeader(AppConstants.USER_UUID_HEADER) String requesterUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<MessageResponse> messagePage = messageService.getMessages(
                conversationId, requesterUuid, page, size);

        PageResponse<MessageResponse> pageResponse = PageResponse.<MessageResponse>builder()
                .content(messagePage.getContent())
                .pageNumber(messagePage.getNumber())
                .pageSize(messagePage.getSize())
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .last(messagePage.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.ok(pageResponse));
    }

    @PostMapping("/messages/{messageId}/delivered")
    public ResponseEntity<ApiResponse<Void>> markDelivered(
            @PathVariable String messageId,
            @RequestHeader(AppConstants.USER_UUID_HEADER) String recipientUuid) {

        deliveryService.markDelivered(messageId, recipientUuid);
        return ResponseEntity.ok(ApiResponse.ok("Marked as delivered", null));
    }

    @PostMapping("/messages/{messageId}/seen")
    public ResponseEntity<ApiResponse<Void>> markSeen(
            @PathVariable String messageId,
            @RequestHeader(AppConstants.USER_UUID_HEADER) String seenByUuid) {

        seenService.markSeen(messageId, seenByUuid);
        return ResponseEntity.ok(ApiResponse.ok("Marked as seen", null));
    }
}

