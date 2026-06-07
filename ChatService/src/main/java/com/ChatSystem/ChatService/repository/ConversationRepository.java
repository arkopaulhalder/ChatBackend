package com.ChatSystem.ChatService.repository;

import com.ChatSystem.ChatService.document.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

    Optional<Conversation> findByConversationId(String conversationId);

    // Find all conversations a user participates in, sorted by last message time
    List<Conversation> findByParticipantIdsContainingOrderByLastMessageAtDesc(String userUuid);

    // Check if a private conversation between two users already exists
    @Query("{ 'participantIds': { $all: [?0, ?1] }, 'type': 'PRIVATE', 'active': true }")
    Optional<Conversation> findPrivateConversation(String userUuid1, String userUuid2);
}
