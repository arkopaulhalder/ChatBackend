package com.ChatSystem.ChatService.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "message_receipts")
@CompoundIndex(name = "idx_message_user", def = "{'messageId': 1, 'userId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageReceipt {

    @Id
    private String id;

    @Indexed(unique = true)
    private String receiptId;

    @Indexed
    private String messageId;

    private String conversationId;

    @Indexed
    private String userId;        // userUuid of the recipient

    @Builder.Default
    private boolean delivered = false;
    private Instant deliveredAt;

    @Builder.Default
    private boolean seen = false;
    private Instant seenAt;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();
}
