package com.testdemo.demo.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class DisruptorConfig {

    @Bean
    public ThreadFactory threadFactory() {
        return Executors.defaultThreadFactory();
    }

    @Bean
    public Disruptor<ValueEvent> disruptor(ThreadFactory threadFactory, ValueEventHandler eventHandler) {
        // 创建disruptor
        Disruptor<ValueEvent> disruptor = new Disruptor<>(
                ValueEvent::new,
                1024,
                threadFactory
        );

        // 连接处理程序
        disruptor.handleEventsWith(eventHandler);

        // 启动disruptor
        disruptor.start();

        return disruptor;
    }

    @Bean
    public RingBuffer<ValueEvent> ringBuffer(Disruptor<ValueEvent> disruptor) {
        return disruptor.getRingBuffer();
    }
} 