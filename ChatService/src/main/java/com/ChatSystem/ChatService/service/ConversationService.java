package com.ChatSystem.ChatService.service;

import com.ChatSystem.ChatService.document.Conversation;
import com.ChatSystem.ChatService.dto.ConversationResponse;
import com.ChatSystem.ChatService.mapper.ChatMapper;
import com.ChatSystem.ChatService.repository.ConversationRepository;
import com.ChatSystem.common_library.enums.ConversationType;
import com.ChatSystem.common_library.exception.BusinessException;
import com.ChatSystem.common_library.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ChatMapper chatMapper;

    public ConversationResponse getOrCreatePrivateConversation(String requesterUuid, String targetUserUuid) {
        if (requesterUuid.equals(targetUserUuid)) {
            throw new BusinessException("Cannot start a conversation with yourself", HttpStatus.BAD_REQUEST.value());
        }

        // Return existing conversation if one already exists — do not create duplicates
        return conversationRepository.findPrivateConversation(requesterUuid, targetUserUuid)
                .map(chatMapper::toConversationResponse)
                .orElseGet(() -> {
                    Conversation newConversation = Conversation.builder()
                            .conversationId(UUID.randomUUID().toString())
                            .type(ConversationType.PRIVATE)
                            .participantIds(List.of(requesterUuid, targetUserUuid))
                            .createdBy(requesterUuid)
                            .build();

                    Conversation saved = conversationRepository.save(newConversation);
                    log.info("Created private conversation {} between {} and {}",
                            saved.getConversationId(), requesterUuid, targetUserUuid);
                    return chatMapper.toConversationResponse(saved);
                });
    }

    public List<ConversationResponse> getUserConversations(String userUuid) {
        return conversationRepository
                .findByParticipantIdsContainingOrderByLastMessageAtDesc(userUuid)
                .stream()
                .map(chatMapper::toConversationResponse)
                .toList();
    }

    public ConversationResponse getConversation(String conversationId, String requesterUuid) {
        Conversation conversation = conversationRepository.findByConversationId(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation", conversationId));

        if (!conversation.getParticipantIds().contains(requesterUuid)) {
            throw new BusinessException("Access denied to this conversation", HttpStatus.FORBIDDEN.value());
        }

        return chatMapper.toConversationResponse(conversation);
    }

    public void updateLastMessage(String conversationId, String messageId,
                                  String messageText, java.time.Instant sentAt) {
        conversationRepository.findByConversationId(conversationId).ifPresent(conv -> {
            conv.setLastMessageId(messageId);
            conv.setLastMessageText(messageText.length() > 100
                    ? messageText.substring(0, 100) + "..." : messageText);
            conv.setLastMessageAt(sentAt);
            conv.setUpdatedAt(java.time.Instant.now());
            conversationRepository.save(conv);
        });
    }
}
