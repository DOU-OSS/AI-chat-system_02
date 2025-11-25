package com.example.aichatsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIModelDTO {
    
    /**
     * 模型ID（用于API调用）
     */
    private String id;
    
    /**
     * 显示名称
     */
    private String name;
    
    /**
     * 是否可用
     */
    private Boolean available;
}
