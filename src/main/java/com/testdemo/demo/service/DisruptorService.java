package com.testdemo.demo.service;

import com.lmax.disruptor.RingBuffer;
import com.testdemo.demo.disruptor.MessageEvent;
import com.testdemo.demo.disruptor.ValueEvent;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DisruptorService {

    private final RingBuffer<ValueEvent> ringBuffer;
    private final MeterRegistry meterRegistry;
    
    private final Counter messagePublishedCounter;
    private final Counter batchPublishedCounter;
    private final Timer messageProcessingTimer;

    public DisruptorService(RingBuffer<ValueEvent> ringBuffer, MeterRegistry meterRegistry) {
        this.ringBuffer = ringBuffer;
        this.meterRegistry = meterRegistry;
        
        this.messagePublishedCounter = Counter.builder("message.published.total")
            .description("Total number of messages published")
            .register(meterRegistry);
            
        this.batchPublishedCounter = Counter.builder("message.batch.published.total")
            .description("Total number of message batches published")
            .register(meterRegistry);
            
        this.messageProcessingTimer = Timer.builder("message.processing.time")
            .description("Time taken to process messages")
            .register(meterRegistry);
    }

    public void publishEvent(MessageEvent message) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            // 获取下一个序列
            long sequence = ringBuffer.next();
            try {
                // 获取该序列对应的事件对象
                ValueEvent event = ringBuffer.get(sequence);
                // 设置事件的值
                event.setMessage(message);
            } finally {
                // 发布事件
                ringBuffer.publish(sequence);
            }
            messagePublishedCounter.increment();
        } finally {
            sample.stop(messageProcessingTimer);
        }
    }

    public void publishEvents(List<MessageEvent> messages) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            messages.forEach(this::publishEvent);
            batchPublishedCounter.increment();
        } finally {
            sample.stop(messageProcessingTimer);
        }
    }

    // 便捷方法：快速发布文本消息
    public void publishTextMessage(String content, String type) {
        MessageEvent message = MessageEvent.builder()
                .id(UUID.randomUUID().toString())
                .content(content)
                .type(type)
                .timestamp(System.currentTimeMillis())
                .build();
        publishEvent(message);
    }
} 