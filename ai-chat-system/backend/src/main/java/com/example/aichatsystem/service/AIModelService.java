package com.example.aichatsystem.service;

import com.example.aichatsystem.config.ModelConfig;
import com.example.aichatsystem.dto.AIModelDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIModelService {
    
    // 简化的动态配置：只需要三个字段 name, URL, key
    // 格式: MODEL_1_NAME, MODEL_1_URL, MODEL_1_KEY
    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.core.env.Environment env;
    
    private ModelConfig modelConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    public void init() {
        loadDynamicModelsFromEnv();
    }
    
    
    /**
     * 从.env动态加载模型配置
     * 只需三个字段：MODEL_X_NAME(模型ID和显示名称), MODEL_X_URL, MODEL_X_KEY
     */
    private void loadDynamicModelsFromEnv() {
        List<ModelConfig.Model> models = new ArrayList<>();
        
        // 最多支持20个模型
        for (int i = 1; i <= 20; i++) {
            String name = env.getProperty("MODEL_" + i + "_NAME");
            String url = env.getProperty("MODEL_" + i + "_URL");
            String key = env.getProperty("MODEL_" + i + "_KEY");
            
            // 如果没有name，说明该位置没有配置
            if (name == null || name.trim().isEmpty()) {
                continue;
            }
            
            // URL和KEY必需
            if (url == null || url.trim().isEmpty() || key == null || key.trim().isEmpty()) {
                log.warn("Model {} configuration incomplete, skipping", i);
                continue;
            }
            
            ModelConfig.Model model = new ModelConfig.Model();
            model.setId(name.trim());  // name既是ID也是显示名称
            model.setName(name.trim());
            model.setUrl(url.trim());
            model.setApiKey(key.trim());
            model.setEnabled(true);
            
            models.add(model);
            log.info("✓ Loaded model {}: {}", i, model.getName());
        }
        
        if (!models.isEmpty()) {
            modelConfig = new ModelConfig();
            modelConfig.setModels(models);
            modelConfig.setDefaultModel(models.get(0).getId());
            log.info("Total {} models configured", models.size());
        } else {
            log.warn("No models configured in .env, using hardcoded configuration");
            modelConfig = createHardcodedModelConfig();
        }
    }
    
    /**
     * 创建硬编码的模型配置作为备用方案
     */
    private ModelConfig createHardcodedModelConfig() {
        List<ModelConfig.Model> models = new ArrayList<>();
        
        // GLM-4.5V
        ModelConfig.Model model1 = new ModelConfig.Model();
        model1.setId("glm-4.5v");
        model1.setName("GLM-4.5V");
        model1.setUrl("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        model1.setApiKey("cf4d75a776814a2d8ba941f4496daa0f.eLMzBdrfOQjZXSLo");
        model1.setEnabled(true);
        models.add(model1);
        
        // GLM-4.5-Air
        ModelConfig.Model model2 = new ModelConfig.Model();
        model2.setId("glm-4.5-air");
        model2.setName("GLM-4.5-Air");
        model2.setUrl("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        model2.setApiKey("cf4d75a776814a2d8ba941f4496daa0f.eLMzBdrfOQjZXSLo");
        model2.setEnabled(true);
        models.add(model2);
        
        // Qwen-Max
        ModelConfig.Model model3 = new ModelConfig.Model();
        model3.setId("qwen3-max");
        model3.setName("Qwen3-Max");
        model3.setUrl("https://dashscope.aliyuncs.com/compatible-mode/v1");
        model3.setApiKey("sk-2a940bccb8e547758145262c840512dd");
        model3.setEnabled(true);
        models.add(model3);
        
        ModelConfig config = new ModelConfig();
        config.setModels(models);
        config.setDefaultModel(models.get(0).getId());
        
        log.info("Created hardcoded configuration with {} models", models.size());
        return config;
    }
    
    
    /**
     * 获取所有可用的AI模型（简化版，只返回name和available状态）
     */
    public List<AIModelDTO> getAvailableModels() {
        List<AIModelDTO> models = new ArrayList<>();
        
        if (modelConfig != null && modelConfig.getModels() != null) {
            for (ModelConfig.Model model : modelConfig.getModels()) {
                // 检查模型是否真正可用
                boolean available = model.isEnabled() && 
                    model.getApiKey() != null && 
                    !model.getApiKey().isEmpty();
                
                models.add(AIModelDTO.builder()
                        .id(model.getId())
                        .name(model.getName())  // 只返回name
                        .available(available)
                        .build());
                
                log.debug("Model: {} - Available: {}", model.getName(), available);
            }
        }
        
        if (models.isEmpty()) {
            log.warn("No models configured in .env file");
        }
        
        return models;
    }
    
    
    /**
     * 获取指定模型的配置
     */
    public ModelConfig.Model getModelConfig(String modelId) {
        if (modelConfig == null || modelConfig.getModels() == null) {
            return null;
        }
        
        return modelConfig.getModels().stream()
            .filter(m -> m.getId().equals(modelId))
            .findFirst()
            .orElse(null);
    }
}
