package com.example.aichatsystem.repository;

import com.example.aichatsystem.entity.Conversation;
import com.example.aichatsystem.entity.Message;
import com.example.aichatsystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);
    Page<Message> findByConversation(Conversation conversation, Pageable pageable);
    List<Message> findTop10ByConversationOrderByCreatedAtDesc(Conversation conversation);
    
    // 统计用户的总消息数（只统计active对话的消息）
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.user = :user AND m.conversation.active = true")
    Long countByConversationUser(@Param("user") User user);
    
    // 统计用户在指定时间范围内的消息数（只统计active对话的消息）
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.user = :user AND m.conversation.active = true AND m.createdAt BETWEEN :startTime AND :endTime")
    Long countByConversationUserAndCreatedAtBetween(@Param("user") User user, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 获取用户的所有消息
    @Query("SELECT m FROM Message m WHERE m.conversation.user = :user ORDER BY m.createdAt DESC")
    List<Message> findByConversationUser(@Param("user") User user);
}
