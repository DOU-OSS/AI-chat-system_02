package com.example.aichatsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDTO {
    
    /**
     * 总对话数
     */
    private Long conversationCount;
    
    /**
     * 总消息数
     */
    private Long messageCount;
    
    /**
     * AI角色数
     */
    private Long roleCount;
    
    /**
     * Token使用量估算
     */
    private Long tokenUsage;
    
    /**
     * 本周活跃度
     */
    private List<DailyActivity> weekActivity;
    
    /**
     * 模型使用统计
     */
    private List<ModelUsage> modelUsage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyActivity {
        private String date;
        private String label;
        private Integer count;
        private Integer percentage;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelUsage {
        private String name;
        private String icon;
        private Long count;
        private Integer percentage;
        private String color;
    }
}
