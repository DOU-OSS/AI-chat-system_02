package com.example.aichatsystem.controller;

import com.example.aichatsystem.entity.User;
import com.example.aichatsystem.repository.UserRepository;
import com.example.aichatsystem.service.ConversationService;
import com.example.aichatsystem.util.ResultUtil;
import com.example.aichatsystem.vo.ConversationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final UserRepository userRepository;

    @PostMapping
    public ResultUtil<ConversationVO> createConversation(
            @RequestBody Map<String, Object> params,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        String title = getStringValue(params.get("title"));
        Long aiRoleId = params.get("aiRoleId") != null ? getLongValue(params.get("aiRoleId")) : null;
        String selectedModel = getStringValue(params.get("selectedModel"));
        
        ConversationVO conversation = conversationService.createConversation(title, aiRoleId, selectedModel, currentUser);
        return ResultUtil.success("Conversation created", conversation);
    }

    @GetMapping
    public ResultUtil<List<ConversationVO>> getUserConversations(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<ConversationVO> conversations = conversationService.getUserConversations(currentUser);
        return ResultUtil.success(conversations);
    }

    @GetMapping("/{id}")
    public ResultUtil<ConversationVO> getConversation(
            @PathVariable Long id,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        ConversationVO conversation = conversationService.getConversation(id, currentUser);
        return ResultUtil.success(conversation);
    }

    @DeleteMapping("/{id}")
    public ResultUtil<Void> deleteConversation(
            @PathVariable Long id,
            @RequestParam(value = "permanent", defaultValue = "false") boolean permanent,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        conversationService.deleteConversation(id, currentUser, permanent);
        String message = permanent ? "Conversation permanently deleted" : "Conversation moved to recycle bin";
        return ResultUtil.success(message, null);
    }
    
    @GetMapping("/deleted")
    public ResultUtil<List<ConversationVO>> getDeletedConversations(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<ConversationVO> conversations = conversationService.getDeletedConversations(currentUser);
        return ResultUtil.success(conversations);
    }
    
    @PostMapping("/{id}/restore")
    public ResultUtil<Void> restoreConversation(
            @PathVariable Long id,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        conversationService.restoreConversation(id, currentUser);
        return ResultUtil.success("Conversation restored", null);
    }
    
    @DeleteMapping("/recycle-bin/empty")
    public ResultUtil<Void> emptyRecycleBin(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        conversationService.emptyRecycleBin(currentUser);
        return ResultUtil.success("Recycle bin emptied", null);
    }

    @PutMapping("/{id}/model")
    public ResultUtil<ConversationVO> updateConversationModel(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        String selectedModel = getStringValue(params.get("selectedModel"));
        
        ConversationVO conversation = conversationService.updateConversationModel(id, selectedModel, currentUser);
        return ResultUtil.success("Model updated", conversation);
    }

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * 安全地从Map中获取String值，处理可能为对象的情况
     */
    private String getStringValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        // 如果是Map（可能是前端传递的对象），尝试获取id字段
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            Object idValue = map.get("id");
            if (idValue != null) {
                return idValue.toString();
            }
            // 如果没有id字段，尝试获取name字段
            Object nameValue = map.get("name");
            if (nameValue != null) {
                return nameValue.toString();
            }
        }
        // 其他情况直接转换为字符串
        return value.toString();
    }
    
    /**
     * 安全地从Map中获取Long值
     */
    private Long getLongValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
