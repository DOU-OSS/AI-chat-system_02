package com.example.aichatsystem.service;

import com.example.aichatsystem.dto.UserStatisticsDTO;
import com.example.aichatsystem.entity.Message;
import com.example.aichatsystem.entity.Conversation;
import com.example.aichatsystem.entity.User;
import com.example.aichatsystem.repository.AIRoleRepository;
import com.example.aichatsystem.repository.MessageRepository;
import com.example.aichatsystem.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStatisticsService {
    
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AIRoleRepository aiRoleRepository;
    
    /**
     * 获取用户的完整统计数据
     */
    public UserStatisticsDTO getUserStatistics(User user) {
        log.info("Getting statistics for user: {}", user.getUsername());
        
        // 获取基本统计（只统计active的对话）
        Long conversationCount = conversationRepository.countByUserAndActive(user, true);
        Long messageCount = messageRepository.countByConversationUser(user);
        Long roleCount = aiRoleRepository.countByUser(user);
        
        // Token使用量估算 (每条消息平均100 tokens)
        Long tokenUsage = messageCount * 100;
        
        // 获取本周活跃度
        List<UserStatisticsDTO.DailyActivity> weekActivity = calculateWeekActivity(user);
        
        // 获取模型使用统计
        List<UserStatisticsDTO.ModelUsage> modelUsage = calculateModelUsage(user);
        
        return UserStatisticsDTO.builder()
                .conversationCount(conversationCount)
                .messageCount(messageCount)
                .roleCount(roleCount)
                .tokenUsage(tokenUsage)
                .weekActivity(weekActivity)
                .modelUsage(modelUsage)
                .build();
    }
    
    /**
     * 计算本周活跃度（过去7天）
     */
    private List<UserStatisticsDTO.DailyActivity> calculateWeekActivity(User user) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        List<UserStatisticsDTO.DailyActivity> activities = new ArrayList<>();
        int maxCount = 0;
        
        // 获取本周每天的消息数
        Map<LocalDate, Integer> dailyCounts = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            Long count = messageRepository.countByConversationUserAndCreatedAtBetween(
                    user, startOfDay, endOfDay);
            
            int messageCount = count.intValue();
            dailyCounts.put(date, messageCount);
            if (messageCount > maxCount) {
                maxCount = messageCount;
            }
        }
        
        // 构建活跃度数据
        String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            int count = dailyCounts.getOrDefault(date, 0);
            int percentage = maxCount > 0 ? (int) ((count * 100.0) / maxCount) : 0;
            
            activities.add(UserStatisticsDTO.DailyActivity.builder()
                    .date(date.format(DateTimeFormatter.ISO_DATE))
                    .label(dayLabels[i])
                    .count(count)
                    .percentage(percentage)
                    .build());
        }
        
        return activities;
    }
    
    /**
     * 计算模型使用统计
     */
    private List<UserStatisticsDTO.ModelUsage> calculateModelUsage(User user) {
        // 获取用户所有active的对话
        List<Conversation> conversations = conversationRepository.findByUserAndActiveOrderByLastMessageAtDesc(user, true);
        
        // 统计每个模型的使用次数
        Map<String, Long> modelCounts = new HashMap<>();
        for (Conversation conv : conversations) {
            // 获取该对话的所有消息
            List<Message> messages = messageRepository.findByConversationOrderByCreatedAtAsc(conv);
            
            for (Message message : messages) {
                if ("USER".equals(message.getRole())) {
                    // 获取AI角色的模型
                    if (conv.getAiRole() != null) {
                        String model = conv.getAiRole().getModel();
                        if (model == null || model.isEmpty()) {
                            model = "gpt-3.5-turbo"; // 默认模型
                        }
                        modelCounts.put(model, modelCounts.getOrDefault(model, 0L) + 1);
                    } else {
                        // 没有指定角色，使用默认模型
                        modelCounts.put("gpt-3.5-turbo", 
                                modelCounts.getOrDefault("gpt-3.5-turbo", 0L) + 1);
                    }
                }
            }
        }
        
        // 计算总数
        long totalCount = modelCounts.values().stream().mapToLong(Long::longValue).sum();
        
        // 转换为DTO并排序
        List<UserStatisticsDTO.ModelUsage> modelUsages = modelCounts.entrySet().stream()
                .map(entry -> {
                    String model = entry.getKey();
                    Long count = entry.getValue();
                    int percentage = totalCount > 0 ? (int) ((count * 100.0) / totalCount) : 0;
                    
                    // 根据模型名称设置图标和颜色
                    String icon = getModelIcon(model);
                    String color = getModelColor(model);
                    String displayName = getModelDisplayName(model);
                    
                    return UserStatisticsDTO.ModelUsage.builder()
                            .name(displayName)
                            .icon(icon)
                            .count(count)
                            .percentage(percentage)
                            .color(color)
                            .build();
                })
                .sorted((a, b) -> b.getCount().compareTo(a.getCount()))
                .limit(5) // 只返回前5个
                .collect(Collectors.toList());
        
        return modelUsages;
    }
    
    /**
     * 获取模型图标
     */
    private String getModelIcon(String model) {
        if (model == null) return "🤖";
        
        if (model.contains("gpt-3.5")) return "🚀";
        if (model.contains("gpt-4-turbo")) return "🔥";
        if (model.contains("gpt-4")) return "⚡";
        if (model.contains("claude")) return "🎯";
        if (model.contains("qwen") || model.contains("通义")) return "🇨🇳";
        
        return "🤖";
    }
    
    /**
     * 获取模型颜色
     */
    private String getModelColor(String model) {
        if (model == null) return "#00bcd4";
        
        if (model.contains("gpt-3.5")) return "#00bcd4";
        if (model.contains("gpt-4-turbo")) return "#f44336";
        if (model.contains("gpt-4")) return "#2196f3";
        if (model.contains("claude")) return "#9c27b0";
        if (model.contains("qwen") || model.contains("通义")) return "#ff9800";
        
        return "#00bcd4";
    }
    
    /**
     * 获取模型显示名称
     */
    private String getModelDisplayName(String model) {
        if (model == null) return "Unknown";
        
        if (model.equals("gpt-3.5-turbo")) return "GPT-3.5 Turbo";
        if (model.equals("gpt-4")) return "GPT-4";
        if (model.equals("gpt-4-turbo")) return "GPT-4 Turbo";
        if (model.equals("claude-3-5-sonnet")) return "Claude 3.5 Sonnet";
        if (model.equals("qwen-turbo")) return "通义千问";
        
        return model;
    }
}
