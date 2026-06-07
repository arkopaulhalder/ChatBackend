package com.ChatSystem.ChatService.document;

import com.ChatSystem.common_library.enums.MessageStatus;
import com.ChatSystem.common_library.enums.MessageType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "messages")
@CompoundIndexes({
        @CompoundIndex(name = "idx_conversation_sent", def = "{'conversationId': 1, 'sentAt': -1}"),
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    private String id;

    @Indexed(unique = true)
    private String messageId;

    @Indexed
    private String conversationId;

    @Indexed
    private String senderId;      // userUuid

    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    private String content;

    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    private String replyToMessageId;  // nullable

    @Builder.Default
    private Instant sentAt = Instant.now();

    private Instant deliveredAt;
    private Instant seenAt;

    @Builder.Default
    private boolean edited = false;
    private Instant editedAt;

    @Builder.Default
    private boolean deleted = false;
    private Instant deletedAt;
}
