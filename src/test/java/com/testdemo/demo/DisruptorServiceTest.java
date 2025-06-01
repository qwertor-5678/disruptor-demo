package com.testdemo.demo;

import com.testdemo.demo.disruptor.MessageEvent;
import com.testdemo.demo.entity.MessageEntity;
import com.testdemo.demo.entity.MessageStatus;
import com.testdemo.demo.repository.MessageRepository;
import com.testdemo.demo.service.DisruptorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DisruptorServiceTest {

    @Autowired
    private DisruptorService disruptorService;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void testPublishEventWithPersistence() throws InterruptedException {
        // 测试发布单个消息事件
        String messageId = UUID.randomUUID().toString();
        MessageEvent singleMessage = MessageEvent.builder()
                .id(messageId)
                .content("测试消息1")
                .type("TEST")
                .timestamp(System.currentTimeMillis())
                .build();
        disruptorService.publishEvent(singleMessage);

        // 等待消息处理完成
        Thread.sleep(1000);

        // 验证消息已被持久化且状态正确
        MessageEntity savedMessage = messageRepository.findById(messageId).orElse(null);
        assertNotNull(savedMessage);
        assertEquals(MessageStatus.COMPLETED, savedMessage.getStatus());
        assertEquals("测试消息1", savedMessage.getContent());

        // 测试发布多个消息事件
        String messageId2 = UUID.randomUUID().toString();
        String messageId3 = UUID.randomUUID().toString();
        
        MessageEvent message2 = MessageEvent.builder()
                .id(messageId2)
                .content("测试消息2")
                .type("TEST")
                .timestamp(System.currentTimeMillis())
                .build();
        MessageEvent message3 = MessageEvent.builder()
                .id(messageId3)
                .content("测试消息3")
                .type("TEST")
                .timestamp(System.currentTimeMillis())
                .build();
        disruptorService.publishEvents(Arrays.asList(message2, message3));

        // 等待消息处理完成
        Thread.sleep(1000);

        // 验证多个消息都已被持久化
        List<MessageEntity> completedMessages = messageRepository.findByTypeAndStatus("TEST", MessageStatus.COMPLETED);
        assertTrue(completedMessages.size() >= 3);
        
        // 验证使用便捷方法发布的消息
        disruptorService.publishTextMessage("快速发布的消息", "QUICK_TEST");
        Thread.sleep(1000);
        
        List<MessageEntity> quickTestMessages = messageRepository.findByTypeAndStatus("QUICK_TEST", MessageStatus.COMPLETED);
        assertFalse(quickTestMessages.isEmpty());
        assertEquals("快速发布的消息", quickTestMessages.get(0).getContent());
    }
} 