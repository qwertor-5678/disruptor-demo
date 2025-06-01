package com.testdemo.demo.disruptor;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEvent {
    private String id;
    private String content;
    private Long timestamp;
    private String type;
} 