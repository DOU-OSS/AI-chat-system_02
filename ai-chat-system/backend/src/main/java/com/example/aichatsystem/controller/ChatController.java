package com.example.aichatsystem.controller;

import com.example.aichatsystem.dto.ChatMessageDTO;
import com.example.aichatsystem.entity.User;
import com.example.aichatsystem.repository.UserRepository;
import com.example.aichatsystem.service.AIService;
import com.example.aichatsystem.util.ResultUtil;
import com.example.aichatsystem.vo.MessageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AIService aiService;
    private final UserRepository userRepository;

    @PostMapping("/send")
    public ResultUtil<MessageVO> sendMessage(
            @Valid @RequestBody ChatMessageDTO messageDTO,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        MessageVO response = aiService.sendMessage(messageDTO, currentUser);
        return ResultUtil.success(response);
    }

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
