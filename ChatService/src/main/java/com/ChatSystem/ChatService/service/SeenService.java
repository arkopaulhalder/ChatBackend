package com.ChatSystem.ChatService.service;


import com.ChatSystem.ChatService.document.ChatMessage;
import com.ChatSystem.ChatService.document.MessageReceipt;
import com.ChatSystem.ChatService.repository.ChatMessageRepository;
import com.ChatSystem.ChatService.repository.MessageReceiptRepository;
import com.ChatSystem.common_library.enums.MessageStatus;
import com.ChatSystem.common_library.event.MessageSeenEvent;
import com.ChatSystem.common_library.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeenService {

    private final ChatMessageRepository messageRepository;
    private final MessageReceiptRepository receiptRepository;
    private final ChatEventPublisher eventPublisher;

    public void markSeen(String messageId, String seenByUuid) {
        ChatMessage message = messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new NotFoundException("Message", messageId));

        MessageReceipt receipt = receiptRepository
                .findByMessageIdAndUserId(messageId, seenByUuid)
                .orElseGet(() -> MessageReceipt.builder()
                        .receiptId(UUID.randomUUID().toString())
                        .messageId(messageId)
                        .conversationId(message.getConversationId())
                        .userId(seenByUuid)
                        .build());

        if (!receipt.isSeen()) {
            Instant now = Instant.now();
            receipt.setSeen(true);
            receipt.setSeenAt(now);
            receipt.setDelivered(true);
            if (receipt.getDeliveredAt() == null) receipt.setDeliveredAt(now);
            receipt.setUpdatedAt(now);
            receiptRepository.save(receipt);

            // SEEN is the terminal status — upgrade unconditionally
            message.setStatus(MessageStatus.SEEN);
            message.setSeenAt(now);
            if (message.getDeliveredAt() == null) message.setDeliveredAt(now);
            messageRepository.save(message);

            eventPublisher.publishMessageSeen(MessageSeenEvent.builder()
                    .messageId(messageId)
                    .conversationId(message.getConversationId())
                    .seenByUuid(seenByUuid)
                    .seenAt(now)
                    .build());

            log.debug("Message seen: messageId={} seenByUuid={}", messageId, seenByUuid);
        }
    }
}
