package com.example.aichatsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRoleDTO {
    @NotBlank(message = "Role name is required")
    private String name;

    private String description;

    @NotBlank(message = "System prompt is required")
    private String systemPrompt;

    private String model = "gpt-3.5-turbo";

    private Boolean isPublic = false;
}
