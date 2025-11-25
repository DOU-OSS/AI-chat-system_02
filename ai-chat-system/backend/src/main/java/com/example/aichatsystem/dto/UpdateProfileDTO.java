package com.example.aichatsystem.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
public class UpdateProfileDTO {
    @Size(min = 1, max = 20, message = "昵称长度必须在1-20个字符之间")
    private String nickname;
    
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;
}
