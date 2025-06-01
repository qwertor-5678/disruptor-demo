package com.testdemo.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "message.processing")
public class MessageProcessingProperties {
    
    /**
     * 批处理大小
     */
    private int batchSize = 100;
    
    /**
     * 重试最大次数
     */
    private int maxRetries = 3;
    
    /**
     * 重试初始延迟（毫秒）
     */
    private long retryInitialDelay = 1000;
    
    /**
     * 重试延迟倍数
     */
    private double retryMultiplier = 2.0;
} 