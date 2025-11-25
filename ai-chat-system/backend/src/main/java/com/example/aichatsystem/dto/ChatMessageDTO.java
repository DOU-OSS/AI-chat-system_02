package com.example.aichatsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    @NotBlank(message = "Message content is required")
    private String content;

    private Long aiRoleId;

    /**
     * 指定要使用的模型ID（来自前端下拉框），可为空
     */
    private String model;
}
