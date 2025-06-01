package com.testdemo.demo.converter;

import com.testdemo.demo.disruptor.MessageEvent;
import com.testdemo.demo.entity.MessageEntity;
import com.testdemo.demo.entity.MessageStatus;

public class MessageConverter {
    
    public static MessageEntity toEntity(MessageEvent event) {
        return MessageEntity.builder()
                .id(event.getId())
                .content(event.getContent())
                .timestamp(event.getTimestamp())
                .type(event.getType())
                .status(MessageStatus.PENDING)
                .processedTimestamp(System.currentTimeMillis())
                .build();
    }

    public static MessageEvent toEvent(MessageEntity entity) {
        return MessageEvent.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .timestamp(entity.getTimestamp())
                .type(entity.getType())
                .build();
    }
} 