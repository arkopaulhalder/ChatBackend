package com.ChatSystem.ChatService.repository;

import com.ChatSystem.ChatService.document.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    Optional<ChatMessage> findByMessageId(String messageId);

    // Paginated message history — compound index on conversationId + sentAt makes this fast
    Page<ChatMessage> findByConversationIdAndDeletedFalseOrderBySentAtDesc(
            String conversationId, Pageable pageable);
}
