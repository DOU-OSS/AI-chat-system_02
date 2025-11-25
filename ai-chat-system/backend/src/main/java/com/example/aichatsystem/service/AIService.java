package com.example.aichatsystem.service;

import com.example.aichatsystem.dto.ChatMessageDTO;
import com.example.aichatsystem.entity.*;
import com.example.aichatsystem.exception.BusinessException;
import com.example.aichatsystem.repository.*;
import com.example.aichatsystem.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AIRoleRepository aiRoleRepository;
    private final RestTemplate restTemplate;
    
    @Autowired
    private AIModelService aiModelService;

    // 最大上下文消息数（避免超过token限制）
    private static final int MAX_CONTEXT_MESSAGES = 20;

    @Transactional
    public MessageVO sendMessage(ChatMessageDTO messageDTO, User currentUser) {
        Conversation conversation = conversationRepository.findByIdAndUser(messageDTO.getConversationId(), currentUser)
                .orElseThrow(() -> new BusinessException("Conversation not found"));

        // Save user message
        Message userMessage = new Message();
        userMessage.setConversation(conversation);
        userMessage.setContent(messageDTO.getContent());
        userMessage.setRole(Message.MessageRole.USER);
        messageRepository.save(userMessage);

        // Get AI response (pass selected model from frontend if provided)
        String aiResponse = getAIResponse(conversation, messageDTO.getContent(), messageDTO.getModel());

        // Save AI message
        Message assistantMessage = new Message();
        assistantMessage.setConversation(conversation);
        assistantMessage.setContent(aiResponse);
        assistantMessage.setRole(Message.MessageRole.ASSISTANT);
        Message savedMessage = messageRepository.save(assistantMessage);

        // Update conversation
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return convertToMessageVO(savedMessage);
    }

    private String getAIResponse(Conversation conversation, String userInput, String requestedModel) {
        try {
            // 获取请求模型的配置
            com.example.aichatsystem.config.ModelConfig.Model modelConfig = null;
            
            if (requestedModel != null && !requestedModel.trim().isEmpty()) {
                modelConfig = aiModelService.getModelConfig(requestedModel.trim());
                if (modelConfig == null) {
                    log.warn("Requested model {} not found in configuration", requestedModel);
                    return "[Error] Model " + requestedModel + " not available. Please select another model.";
                }
            } else {
                // 使用默认配置的第一个可用模型
                List<com.example.aichatsystem.dto.AIModelDTO> availableModels = aiModelService.getAvailableModels();
                for (com.example.aichatsystem.dto.AIModelDTO dto : availableModels) {
                    if (dto.getAvailable() && !"demo".equals(dto.getId())) {
                        modelConfig = aiModelService.getModelConfig(dto.getId());
                        break;
                    }
                }
            }
            
            if (modelConfig == null) {
                log.warn("No valid model configuration available");
                return "[System] No AI models configured. Please check your API keys in .env file.";
            }
            
            String apiKey = modelConfig.getApiKey();
            String baseUrl = modelConfig.getUrl();
            String actualModel = modelConfig.getId();
            
            if (apiKey == null || apiKey.isEmpty() || apiKey.contains("your-") || apiKey.contains("${")) {
                log.warn("API key not configured properly for model {}", actualModel);
                return "[System] API key for " + modelConfig.getName() + " not configured. Please set the API key in .env file.";
            }

            log.info("Calling AI API: model={} ({}), url={}", actualModel, modelConfig.getProvider(), baseUrl);
            
            // 获取AI角色的系统提示
            String systemPrompt = "You are a helpful assistant.";
            if (conversation.getAiRole() != null) {
                systemPrompt = conversation.getAiRole().getSystemPrompt();
            }
            
            // 检查是否启用thinking模式
            boolean isThinkingMode = false;
            String cleanedInput = userInput;
            if (userInput.startsWith("[THINKING_MODE]\n")) {
                isThinkingMode = true;
                cleanedInput = userInput.substring("[THINKING_MODE]\n".length());
            }
            
            // 获取对话历史消息（上下文记忆）
            List<Message> historyMessages = messageRepository.findByConversationOrderByCreatedAtAsc(conversation);
            
            // 统一使用OpenAI兼容格式调用
            String response = callUnifiedAPI(apiKey, baseUrl, actualModel, systemPrompt, cleanedInput, isThinkingMode, historyMessages);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error calling AI service: ", e);
            return "[Error] Failed to get AI response: " + e.getMessage();
        }
    }
    
    /**
     * 统一的API调用方法（兼容OpenAI格式）
     * @param enableThinking 是否启用深度思考模式（仅通义千问支持）
     * @param historyMessages 历史消息（上下文记忆）
     */
    private String callUnifiedAPI(String apiKey, String baseUrl, String model, String systemPrompt, String userInput, boolean enableThinking, List<Message> historyMessages) {
        try {
            // 智谱AI使用JWT token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            
            List<Map<String, String>> messages = new ArrayList<>();
            // 添加系统提示
            messages.add(Map.of("role", "system", "content", systemPrompt));
            
            // 添加历史消息（上下文记忆）
            if (historyMessages != null && !historyMessages.isEmpty()) {
                // 只取最近的N条消息，避免超过token限制
                int startIndex = Math.max(0, historyMessages.size() - MAX_CONTEXT_MESSAGES);
                List<Message> recentMessages = historyMessages.subList(startIndex, historyMessages.size());
                
                for (Message msg : recentMessages) {
                    String role = msg.getRole() == Message.MessageRole.USER ? "user" : "assistant";
                    String content = msg.getContent();
                    
                    // 移除历史消息中的thinking标记和格式，只保留纯净内容
                    if (content.startsWith("[THINKING_MODE]\n")) {
                        content = content.substring("[THINKING_MODE]\n".length());
                    }
                    // 移除thinking标签，只保留answer内容
                    if (content.contains("<thinking>") && content.contains("<answer>")) {
                        int answerStart = content.indexOf("<answer>");
                        int answerEnd = content.indexOf("</answer>");
                        if (answerStart != -1 && answerEnd != -1) {
                            content = content.substring(answerStart + 8, answerEnd).trim();
                        }
                    }
                    
                    messages.add(Map.of("role", role, "content", content));
                }
                
                log.info("📚 Loading {} history messages as context", recentMessages.size());
            }
            
            // 添加当前用户输入
            messages.add(Map.of("role", "user", "content", userInput));
            requestBody.put("messages", messages);
            
            requestBody.put("stream", false);
            
            // 如果启用thinking模式且是通义千问模型，添加enable_thinking参数
            if (enableThinking && (model.toLowerCase().contains("qwen") || model.toLowerCase().contains("qianwen"))) {
                Map<String, Object> extraBody = new HashMap<>();
                extraBody.put("enable_thinking", true);
                // 将extra_body参数直接添加到requestBody顶层
                requestBody.put("enable_thinking", true);
                log.info("🤔 Enabling Qwen Deep Thinking Mode for model: {}", model);
            }
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // 确保URL格式正确
            String apiUrl = baseUrl;
            if (!apiUrl.endsWith("/chat/completions")) {
                if (!apiUrl.endsWith("/")) {
                    apiUrl += "/";
                }
                if (!apiUrl.contains("/chat/completions")) {
                    apiUrl += "chat/completions";
                }
            }
            
            log.info("Calling API: {} with model: {}", apiUrl, model);
            
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                apiUrl,
                request,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = response.getBody();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    
                    // 检查是否有思考过程（通义千问深度思考模式）
                    String reasoning = getStringFromMap(message, "reasoning_content");
                    String content = getStringFromMap(message, "content");
                    
                    // 只有当用户明确启用thinking模式时，才返回thinking格式
                    if (enableThinking && reasoning != null && !reasoning.isEmpty()) {
                        // 使用特殊格式返回，前端可以解析
                        log.info("🤔 Thinking mode response with {} chars reasoning", reasoning.length());
                        return "<thinking>" + reasoning + "</thinking><answer>" + content + "</answer>";
                    }
                    
                    // 普通模式，只返回内容
                    return content;
                }
            }
            
            return "[Error] No response from AI service";
            
        } catch (Exception e) {
            log.error("Error calling API: ", e);
            return "[Error] AI service error: " + e.getMessage();
        }
    }

    private MessageVO convertToMessageVO(Message message) {
        MessageVO messageVO = new MessageVO();
        messageVO.setId(message.getId());
        messageVO.setContent(message.getContent());
        messageVO.setRole(message.getRole().toString());
        messageVO.setTokenCount(message.getTokenCount());
        messageVO.setCreatedAt(message.getCreatedAt());
        return messageVO;
    }
    
    /**
     * 安全地从Map中获取String值，处理可能为对象的情况
     */
    private String getStringFromMap(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return null;
        }
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        // 如果是Map（嵌套对象），尝试获取id或name字段
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nestedMap = (Map<String, Object>) value;
            Object idValue = nestedMap.get("id");
            if (idValue != null) {
                return idValue.toString();
            }
            Object nameValue = nestedMap.get("name");
            if (nameValue != null) {
                return nameValue.toString();
            }
        }
        // 其他情况直接转换为字符串
        return value.toString();
    }
}
