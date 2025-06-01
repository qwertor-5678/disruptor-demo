package com.testdemo.demo.health;

import com.testdemo.demo.entity.MessageStatus;
import com.testdemo.demo.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageProcessingHealthIndicator implements HealthIndicator {

    private final MessageRepository messageRepository;
    private static final long MAX_FAILED_MESSAGES = 1000; // 最大允许的失败消息数
    private static final long MAX_PENDING_TIME = 300000; // 最大允许的待处理时间（5分钟）

    @Override
    public Health health() {
        try {
            // 检查失败的消息数量
            long failedCount = messageRepository.findByStatus(MessageStatus.FAILED).size();
            
            // 检查是否有长时间未处理的消息
            long currentTime = System.currentTimeMillis();
            long oldPendingCount = messageRepository.countOldPendingMessages(currentTime - MAX_PENDING_TIME);
            
            Health.Builder builder = Health.up()
                .withDetail("failedMessages", failedCount)
                .withDetail("oldPendingMessages", oldPendingCount);
            
            if (failedCount > MAX_FAILED_MESSAGES) {
                return builder.down()
                    .withDetail("error", "Too many failed messages: " + failedCount)
                    .build();
            }
            
            if (oldPendingCount > 0) {
                return builder.down()
                    .withDetail("error", "Messages stuck in PENDING state: " + oldPendingCount)
                    .build();
            }
            
            return builder.build();
            
        } catch (Exception e) {
            return Health.down()
                .withException(e)
                .build();
        }
    }
} 