package com.example.aichatsystem.service;

import com.example.aichatsystem.entity.Conversation;
import com.example.aichatsystem.entity.Message;
import com.example.aichatsystem.exception.BusinessException;
import com.example.aichatsystem.repository.ConversationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {
    
    private final ConversationRepository conversationRepository;
    private final ObjectMapper objectMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Transactional(readOnly = true)
    public byte[] exportConversationAsText(Long conversationId, String username) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException("Conversation not found"));
        
        if (!conversation.getUser().getUsername().equals(username)) {
            throw new BusinessException("You don't have permission to export this conversation");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("====================================\n");
        sb.append("对话导出\n");
        sb.append("====================================\n\n");
        sb.append("对话标题: ").append(conversation.getTitle()).append("\n");
        sb.append("AI角色: ").append(conversation.getAiRole().getName()).append("\n");
        sb.append("创建时间: ").append(conversation.getCreatedAt().format(DATE_FORMATTER)).append("\n");
        sb.append("消息数量: ").append(conversation.getMessages().size()).append("\n");
        sb.append("\n====================================\n\n");
        
        List<Message> messages = conversation.getMessages().stream()
                .sorted((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()))
                .collect(Collectors.toList());
        
        for (Message message : messages) {
            sb.append("[").append(message.getCreatedAt().format(DATE_FORMATTER)).append("] ");
            sb.append(message.getRole().name()).append(":\n");
            sb.append(message.getContent()).append("\n\n");
            sb.append("---\n\n");
        }
        
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    @Transactional(readOnly = true)
    public byte[] exportConversationAsJson(Long conversationId, String username) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException("Conversation not found"));
        
        if (!conversation.getUser().getUsername().equals(username)) {
            throw new BusinessException("You don't have permission to export this conversation");
        }
        
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("conversationId", conversation.getId());
        exportData.put("title", conversation.getTitle());
        exportData.put("aiRole", conversation.getAiRole().getName());
        exportData.put("createdAt", conversation.getCreatedAt().format(DATE_FORMATTER));
        exportData.put("updatedAt", conversation.getUpdatedAt().format(DATE_FORMATTER));
        
        List<Map<String, Object>> messages = conversation.getMessages().stream()
                .sorted((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()))
                .map(message -> {
                    Map<String, Object> msgData = new HashMap<>();
                    msgData.put("role", message.getRole().name().toLowerCase());
                    msgData.put("content", message.getContent());
                    msgData.put("timestamp", message.getCreatedAt().format(DATE_FORMATTER));
                    return msgData;
                })
                .collect(Collectors.toList());
        
        exportData.put("messages", messages);
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(baos, exportData);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to export conversation as JSON", e);
            throw new BusinessException("Failed to export conversation");
        }
    }
    
    @Transactional(readOnly = true)
    public byte[] exportConversationAsMarkdown(Long conversationId, String username) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException("Conversation not found"));
        
        if (!conversation.getUser().getUsername().equals(username)) {
            throw new BusinessException("You don't have permission to export this conversation");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(conversation.getTitle()).append("\n\n");
        sb.append("**AI角色:** ").append(conversation.getAiRole().getName()).append("\n\n");
        sb.append("**创建时间:** ").append(conversation.getCreatedAt().format(DATE_FORMATTER)).append("\n\n");
        sb.append("**消息数量:** ").append(conversation.getMessages().size()).append("\n\n");
        sb.append("---\n\n");
        
        List<Message> messages = conversation.getMessages().stream()
                .sorted((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()))
                .collect(Collectors.toList());
        
        for (Message message : messages) {
            if (Message.MessageRole.USER.equals(message.getRole())) {
                sb.append("### 👤 用户\n");
            } else if (Message.MessageRole.ASSISTANT.equals(message.getRole())) {
                sb.append("### 🤖 AI助手\n");
            } else {
                sb.append("### 📋 系统\n");
            }
            
            sb.append("*").append(message.getCreatedAt().format(DATE_FORMATTER)).append("*\n\n");
            sb.append(message.getContent()).append("\n\n");
        }
        
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
