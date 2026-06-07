package com.ChatSystem.ChatService.repository;

import com.ChatSystem.ChatService.document.MessageReceipt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MessageReceiptRepository extends MongoRepository<MessageReceipt, String> {

    Optional<MessageReceipt> findByMessageIdAndUserId(String messageId, String userId);

    List<MessageReceipt> findByMessageId(String messageId);

    List<MessageReceipt> findByConversationIdAndUserId(String conversationId, String userId);
}