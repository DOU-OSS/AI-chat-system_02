package com.example.aichatsystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {
    private Long id;
    private String content;
    private String role;
    private Integer tokenCount;
    private LocalDateTime createdAt;
}
