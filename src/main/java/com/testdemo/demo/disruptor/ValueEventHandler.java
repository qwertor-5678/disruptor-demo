package com.testdemo.demo.disruptor;

import com.lmax.disruptor.EventHandler;
import com.testdemo.demo.converter.MessageConverter;
import com.testdemo.demo.entity.MessageEntity;
import com.testdemo.demo.service.MessageProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ValueEventHandler implements EventHandler<ValueEvent> {
    
    private final MessageProcessingService messageProcessingService;
    private final List<MessageEntity> batchMessages = new ArrayList<>();
    private static final int BATCH_SIZE = 100;
    
    @Override
    public void onEvent(ValueEvent event, long sequence, boolean endOfBatch) {
        MessageEvent message = event.getMessage();
        log.debug("Received message - Sequence: {} Message: [id={}, type={}]", 
                sequence, message.getId(), message.getType());
                
        try {
            // 转换消息
            MessageEntity entity = MessageConverter.toEntity(message);
            
            // 添加到批处理列表
            batchMessages.add(entity);
            
            // 如果达到批处理大小或是批次结束，则处理批次
            if (batchMessages.size() >= BATCH_SIZE || endOfBatch) {
                processBatch();
            }
            
        } catch (Exception e) {
            log.error("Error handling message: " + message.getId(), e);
        }
    }

    private void processBatch() {
        if (batchMessages.isEmpty()) {
            return;
        }

        try {
            // 创建新的列表进行处理，避免并发问题
            List<MessageEntity> messagesToProcess = new ArrayList<>(batchMessages);
            batchMessages.clear();
            
            // 使用服务处理批次
            messageProcessingService.processBatch(messagesToProcess);
            
        } catch (Exception e) {
            log.error("Error processing batch", e);
        }
    }
} 
