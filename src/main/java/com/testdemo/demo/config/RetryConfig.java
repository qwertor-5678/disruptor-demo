package com.testdemo.demo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRetry
@RequiredArgsConstructor
@Slf4j
public class RetryConfig {

    private final MessageProcessingProperties properties;

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 配置重试策略
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(properties.getMaxRetries());
        retryTemplate.setRetryPolicy(retryPolicy);

        // 配置退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(properties.getRetryInitialDelay());
        backOffPolicy.setMultiplier(properties.getRetryMultiplier());
        backOffPolicy.setMaxInterval(10000L); // 最大间隔10秒
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 添加重试监听器
        retryTemplate.registerListener(new RetryListener() {
            @Override
            public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
                log.debug("Starting retry operation for context: {}", context.getAttribute(RetryContext.NAME));
                return true;
            }

            @Override
            public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                log.debug("Retry operation completed for context: {}, successful: {}", 
                    context.getAttribute(RetryContext.NAME),
                    context.getRetryCount() < properties.getMaxRetries());
            }

            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                log.warn("Retry attempt {} failed with exception: {}", 
                    context.getRetryCount(),
                    throwable.getMessage());
            }
        });

        return retryTemplate;
    }
} 