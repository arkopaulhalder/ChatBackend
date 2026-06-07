package com.ChatSystem.ChatService.mapper;

import com.ChatSystem.ChatService.document.ChatMessage;
import com.ChatSystem.ChatService.document.Conversation;
import com.ChatSystem.ChatService.dto.ConversationResponse;
import com.ChatSystem.ChatService.dto.MessageResponse;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    public MessageResponse toMessageResponse(ChatMessage message) {
        return MessageResponse.builder()
                .messageId(message.getMessageId())
                .conversationId(message.getConversationId())
                .senderId(message.getSenderId())
                .messageType(message.getMessageType())
                .content(message.isDeleted() ? null : message.getContent()) // hide content if deleted
                .status(message.getStatus())
                .replyToMessageId(message.getReplyToMessageId())
                .sentAt(message.getSentAt())
                .deliveredAt(message.getDeliveredAt())
                .seenAt(message.getSeenAt())
                .edited(message.isEdited())
                .deleted(message.isDeleted())
                .build();
    }

    public ConversationResponse toConversationResponse(Conversation conversation) {
        return ConversationResponse.builder()
                .conversationId(conversation.getConversationId())
                .type(conversation.getType())
                .participantIds(conversation.getParticipantIds())
                .lastMessageText(conversation.getLastMessageText())
                .lastMessageAt(conversation.getLastMessageAt())
                .createdAt(conversation.getCreatedAt())
                .build();
    }
}