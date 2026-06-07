package com.ChatSystem.ChatService.consumer;

import com.ChatSystem.ChatService.document.ChatMessage;
import com.ChatSystem.ChatService.document.Conversation;
import com.ChatSystem.ChatService.repository.ChatMessageRepository;
import com.ChatSystem.ChatService.repository.ConversationRepository;
import com.ChatSystem.ChatService.repository.MessageReceiptRepository;
import com.ChatSystem.common_library.constants.KafkaTopics;
import com.ChatSystem.common_library.event.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletedEventConsumer {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final MessageReceiptRepository receiptRepository;
    private final MongoTemplate mongoTemplate;

    @KafkaListener(
            topics = KafkaTopics.USER_DELETED,
            groupId = "chat-service-delete-group",
            containerFactory = "userDeletedKafkaListenerContainerFactory"
    )
    public void consume(UserDeletedEvent event) {
        String uuid = event.getUserUuid();
        log.info("Processing chat data deletion for uuid={}", uuid);

        try {
            // 1. Find all conversations this user is part of
            List<Conversation> conversations = conversationRepository
                    .findByParticipantIdsContainingOrderByLastMessageAtDesc(uuid);

            List<String> conversationIds = conversations.stream()
                    .map(Conversation::getConversationId)
                    .toList();

            // 2. Delete all message receipts for this user
            mongoTemplate.remove(
                    Query.query(Criteria.where("userId").is(uuid)),
                    "message_receipts"
            );

            // 3. Delete all messages sent by this user
            mongoTemplate.remove(
                    Query.query(Criteria.where("senderId").is(uuid)),
                    "messages"
            );

            // 4. Delete all messages in conversations this user was part of
            if (!conversationIds.isEmpty()) {
                mongoTemplate.remove(
                        Query.query(Criteria.where("conversationId").in(conversationIds)),
                        "messages"
                );

                // 5. Delete all receipts in those conversations
                mongoTemplate.remove(
                        Query.query(Criteria.where("conversationId").in(conversationIds)),
                        "message_receipts"
                );
            }

            // 6. Delete all conversations this user was part of
            conversationRepository.deleteAll(conversations);

            log.info("ChatService data deleted for uuid={} — {} conversations removed",
                    uuid, conversations.size());

        } catch (Exception e) {
            log.error("Failed to delete ChatService data for uuid={}: {}",
                    uuid, e.getMessage(), e);
        }
    }
}
