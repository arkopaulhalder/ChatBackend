package com.ChatSystem.ChatService.config;


import com.ChatSystem.ChatService.document.ChatMessage;
import com.ChatSystem.ChatService.document.Conversation;
import com.ChatSystem.ChatService.document.MessageReceipt;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;

import org.bson.Document;

/**
 * Creates MongoDB indexes on startup.
 *
 * We do this programmatically instead of relying on @Indexed auto-creation
 * because auto-creation only runs once per JVM start and does not guarantee
 * creation order or allow compound options like background builds.
 *
 * If an index already exists with the same key + options, ensureIndex is a no-op.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void ensureIndexes() {
        log.info("Ensuring MongoDB indexes...");

        // conversations
        mongoTemplate.indexOps(Conversation.class)
                .ensureIndex(new Index().on("conversationId", Sort.Direction.ASC).unique());
        mongoTemplate.indexOps(Conversation.class)
                .ensureIndex(new Index().on("participantIds", Sort.Direction.ASC));

        // messages
        mongoTemplate.indexOps(ChatMessage.class)
                .ensureIndex(new Index().on("messageId", Sort.Direction.ASC).unique());
        mongoTemplate.indexOps(ChatMessage.class)
                .ensureIndex(new Index().on("conversationId", Sort.Direction.ASC));
        mongoTemplate.indexOps(ChatMessage.class)
                .ensureIndex(new Index().on("senderId", Sort.Direction.ASC));
        // Compound index for paginated history queries
        mongoTemplate.indexOps(ChatMessage.class)
                .ensureIndex(new CompoundIndexDefinition(
                        new Document("conversationId", 1).append("sentAt", -1)));

        // message_receipts
        mongoTemplate.indexOps(MessageReceipt.class)
                .ensureIndex(new Index().on("receiptId", Sort.Direction.ASC).unique());
        mongoTemplate.indexOps(MessageReceipt.class)
                .ensureIndex(new Index().on("messageId", Sort.Direction.ASC));
        mongoTemplate.indexOps(MessageReceipt.class)
                .ensureIndex(new Index().on("userId", Sort.Direction.ASC));
        mongoTemplate.indexOps(MessageReceipt.class)
                .ensureIndex(new CompoundIndexDefinition(
                        new Document("messageId", 1).append("userId", 1)).unique());

        log.info("MongoDB indexes ensured.");
    }
}
