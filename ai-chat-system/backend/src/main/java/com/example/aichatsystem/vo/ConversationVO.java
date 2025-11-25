package com.example.aichatsystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationVO {
    private Long id;
    private String title;
    private Long aiRoleId;
    private String aiRoleName;
    private String aiRoleDescription; // AI角色的描述
    private String selectedModel; // 用户选择的AI模型
    private List<MessageVO> messages;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private LocalDateTime deletedAt;
}
