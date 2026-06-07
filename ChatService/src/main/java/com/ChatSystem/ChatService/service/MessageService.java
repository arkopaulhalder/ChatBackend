package com.ChatSystem.ChatService.service;


import com.ChatSystem.ChatService.document.ChatMessage;
import com.ChatSystem.ChatService.document.Conversation;
import com.ChatSystem.ChatService.dto.MessageResponse;
import com.ChatSystem.ChatService.dto.SendMessageRequest;
import com.ChatSystem.ChatService.mapper.ChatMapper;
import com.ChatSystem.ChatService.repository.ChatMessageRepository;
import com.ChatSystem.ChatService.repository.ConversationRepository;
import com.ChatSystem.common_library.enums.MessageStatus;
import com.ChatSystem.common_library.enums.MessageType;
import com.ChatSystem.common_library.event.MessageCreatedEvent;
import com.ChatSystem.common_library.exception.BusinessException;
import com.ChatSystem.common_library.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final ChatMessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;
    private final ChatEventPublisher eventPublisher;
    private final ChatMapper chatMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageResponse sendMessage(SendMessageRequest request, String senderUuid) {
        Conversation conversation = conversationRepository
                .findByConversationId(request.getConversationId())
                .orElseThrow(() -> new NotFoundException("Conversation", request.getConversationId()));

        // Verify sender is actually a participant — never trust the client alone
        if (!conversation.getParticipantIds().contains(senderUuid)) {
            throw new BusinessException("You are not a participant in this conversation", HttpStatus.FORBIDDEN.value());
        }

        ChatMessage message = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .conversationId(request.getConversationId())
                .senderId(senderUuid)
                .messageType(MessageType.TEXT)
                .content(request.getContent())
                .status(MessageStatus.SENT)
                .replyToMessageId(request.getReplyToMessageId())
                .sentAt(Instant.now())
                .build();

        ChatMessage saved = messageRepository.save(message);

        // Update conversation's last message preview
        conversationService.updateLastMessage(
                saved.getConversationId(),
                saved.getMessageId(),
                saved.getContent(),
                saved.getSentAt()
        );

        MessageResponse response = chatMapper.toMessageResponse(saved);

        // Push to all conversation subscribers via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/conversation." + saved.getConversationId(), response);

        // Also push to each recipient's personal queue (for multi-device support)
        conversation.getParticipantIds().stream()
                .filter(uid -> !uid.equals(senderUuid))
                .forEach(recipientUuid ->
                        messagingTemplate.convertAndSendToUser(
                                recipientUuid, "/queue/messages", response));

        // Publish Kafka event for downstream consumers (NotificationService, etc.)
        String recipientUuid = conversation.getParticipantIds().stream()
                .filter(uid -> !uid.equals(senderUuid))
                .findFirst()
                .orElse(null);

        eventPublisher.publishMessageCreated(MessageCreatedEvent.builder()
                .messageId(saved.getMessageId())
                .conversationId(saved.getConversationId())
                .senderUuid(senderUuid)
                .recipientUuid(recipientUuid)
                .messageType(saved.getMessageType())
                .content(saved.getContent())
                .sentAt(saved.getSentAt())
                .build());

        log.debug("Message sent: messageId={} conversationId={}", saved.getMessageId(), saved.getConversationId());
        return response;
    }

    public Page<MessageResponse> getMessages(String conversationId, String requesterUuid,
                                             int page, int size) {
        Conversation conversation = conversationRepository
                .findByConversationId(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation", conversationId));

        if (!conversation.getParticipantIds().contains(requesterUuid)) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN.value());
        }

        return messageRepository
                .findByConversationIdAndDeletedFalseOrderBySentAtDesc(
                        conversationId, PageRequest.of(page, size))
                .map(chatMapper::toMessageResponse);
    }
}
