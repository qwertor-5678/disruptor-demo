package com.testdemo.demo.service;

import com.testdemo.demo.entity.MessageEntity;
import com.testdemo.demo.entity.MessageStatus;
import com.testdemo.demo.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageProcessingService {
    
    private final MessageRepository messageRepository;
    private final RetryTemplate retryTemplate;

    @Transactional
    public void processBatch(List<MessageEntity> messages) {
        if (messages.isEmpty()) {
            return;
        }

        retryTemplate.execute(context -> {
            try {
                // 保存所有消息并设置状态为处理中
                messages.forEach(msg -> msg.setStatus(MessageStatus.PROCESSING));
                List<MessageEntity> savedMessages = messageRepository.saveAll(messages);
                
                // 处理每条消息
                for (MessageEntity message : savedMessages) {
                    processMessage(message);
                }
                
                // 更新状态为完成
                savedMessages.forEach(msg -> {
                    msg.setStatus(MessageStatus.COMPLETED);
                    msg.setProcessedTimestamp(System.currentTimeMillis());
                });
                messageRepository.saveAll(savedMessages);
                
                return null;
            } catch (Exception e) {
                log.error("Batch processing failed", e);
                handleBatchError(messages, e);
                throw e;
            }
        });
    }

    @Transactional
    public void handleBatchError(List<MessageEntity> messages, Exception e) {
        messages.forEach(msg -> {
            msg.setStatus(MessageStatus.FAILED);
            msg.setErrorMessage(e.getMessage());
            msg.setProcessedTimestamp(System.currentTimeMillis());
        });
        messageRepository.saveAll(messages);
    }

    private void processMessage(MessageEntity message) {
        try {
            // 这里是消息处理的具体业务逻辑
            // 目前只是模拟处理
            Thread.sleep(1L);
            log.debug("Processed message: {}", message.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Message processing interrupted", e);
        }
    }

    @Transactional(readOnly = true)
    public List<MessageEntity> findFailedMessages() {
        return messageRepository.findByStatus(MessageStatus.FAILED);
    }

    @Transactional(readOnly = true)
    public List<MessageEntity> findMessagesByTypeAndStatus(String type, MessageStatus status) {
        return messageRepository.findByTypeAndStatus(type, status);
    }
} 