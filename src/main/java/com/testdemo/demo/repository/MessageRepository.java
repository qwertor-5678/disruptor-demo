package com.testdemo.demo.repository;

import com.testdemo.demo.entity.MessageEntity;
import com.testdemo.demo.entity.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, String> {
    List<MessageEntity> findByStatus(MessageStatus status);
    List<MessageEntity> findByTypeAndStatus(String type, MessageStatus status);
    
    @Query("SELECT COUNT(m) FROM MessageEntity m WHERE m.status = 'PENDING' AND m.timestamp < :threshold")
    long countOldPendingMessages(@Param("threshold") Long threshold);
} 