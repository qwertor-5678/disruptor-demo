package com.testdemo.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_type_status", columnList = "type,status"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {
    @Id
    private String id;
    
    @Column(nullable = false, length = 1000)
    private String content;
    
    @Column(nullable = false)
    private Long timestamp;
    
    @Column(nullable = false, length = 50)
    private String type;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MessageStatus status;
    
    @Column(length = 500)
    private String errorMessage;
    
    @Column(nullable = false)
    private Long processedTimestamp;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Long createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Long updatedAt;

    @Version
    private Long version;
} 