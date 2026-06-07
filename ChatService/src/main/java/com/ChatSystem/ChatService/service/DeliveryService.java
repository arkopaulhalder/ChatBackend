package com.ChatSystem.ChatService.service;


import com.ChatSystem.ChatService.document.ChatMessage;
import com.ChatSystem.ChatService.document.MessageReceipt;
import com.ChatSystem.ChatService.repository.ChatMessageRepository;
import com.ChatSystem.ChatService.repository.MessageReceiptRepository;
import com.ChatSystem.common_library.enums.MessageStatus;
import com.ChatSystem.common_library.event.MessageDeliveredEvent;
import com.ChatSystem.common_library.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final ChatMessageRepository messageRepository;
    private final MessageReceiptRepository receiptRepository;
    private final ChatEventPublisher eventPublisher;

    public void markDelivered(String messageId, String recipientUuid) {
        ChatMessage message = messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new NotFoundException("Message", messageId));

        // Upsert receipt — idempotent, calling twice is safe
        MessageReceipt receipt = receiptRepository
                .findByMessageIdAndUserId(messageId, recipientUuid)
                .orElseGet(() -> MessageReceipt.builder()
                        .receiptId(UUID.randomUUID().toString())
                        .messageId(messageId)
                        .conversationId(message.getConversationId())
                        .userId(recipientUuid)
                        .build());

        if (!receipt.isDelivered()) {
            receipt.setDelivered(true);
            receipt.setDeliveredAt(Instant.now());
            receipt.setUpdatedAt(Instant.now());
            receiptRepository.save(receipt);

            // Update message status to DELIVERED if not already SEEN
            if (message.getStatus() == MessageStatus.SENT) {
                message.setStatus(MessageStatus.DELIVERED);
                message.setDeliveredAt(receipt.getDeliveredAt());
                messageRepository.save(message);
            }

            eventPublisher.publishMessageDelivered(MessageDeliveredEvent.builder()
                    .messageId(messageId)
                    .conversationId(message.getConversationId())
                    .recipientUuid(recipientUuid)
                    .deliveredAt(receipt.getDeliveredAt())
                    .build());

            log.debug("Message delivered: messageId={} recipientUuid={}", messageId, recipientUuid);
        }
    }
}
