package com.example.aichatsystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRoleVO {
    private Long id;
    private String name;
    private String description;
    private String systemPrompt;
    private String model;
    private Boolean isPublic;
    private LocalDateTime createdAt;
}
