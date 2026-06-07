package com.ChatSystem.ChatService.document;

import com.ChatSystem.common_library.enums.ConversationType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "conversations")
@CompoundIndex(name = "idx_participants", def = "{'participant_ids': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    private String id;

    @Indexed(unique = true)
    private String conversationId;

    @Builder.Default
    private ConversationType type = ConversationType.PRIVATE;

    private List<String> participantIds;  // list of userUuids

    private String createdBy;             // userUuid of creator

    private String lastMessageId;
    private String lastMessageText;
    private Instant lastMessageAt;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();
}
