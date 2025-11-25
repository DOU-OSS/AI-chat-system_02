package com.example.aichatsystem.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * .env 文件自动加载配置
 * 在应用启动时自动读取项目根目录的 .env 文件
 */
@Slf4j
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // 查找 .env 文件（支持多个位置）
            String[] possiblePaths = {
                "../.env",              // backend/../.env (项目根目录)
                ".env",                 // backend/.env
                "../../.env"            // backend/../../.env
            };

            Dotenv dotenv = null;
            String foundPath = null;

            for (String path : possiblePaths) {
                File envFile = new File(path);
                if (envFile.exists()) {
                    try {
                        dotenv = Dotenv.configure()
                                .directory(envFile.getParent() != null ? envFile.getParent() : ".")
                                .filename(envFile.getName())
                                .ignoreIfMissing()
                                .load();
                        foundPath = envFile.getAbsolutePath();
                        break;
                    } catch (Exception e) {
                        log.debug("Failed to load .env from {}: {}", path, e.getMessage());
                    }
                }
            }

            if (dotenv == null) {
                log.info("No .env file found, using default configuration or system environment variables");
                return;
            }

            // 将 .env 中的变量添加到 Spring Environment
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            Map<String, Object> envMap = new HashMap<>();
            
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                envMap.put(key, value);
                
                // 同时设置为系统环境变量（可选）
                System.setProperty(key, value);
            });

            environment.getPropertySources().addFirst(
                    new MapPropertySource("dotenvProperties", envMap)
            );

            log.info("✓ Loaded .env file from: {}", foundPath);
            log.info("✓ Loaded {} environment variables", envMap.size());
            
            // 打印已加载的 AI 服务配置（不显示密钥）
            printLoadedAIServices(envMap);
            
        } catch (Exception e) {
            log.warn("Failed to load .env file: {}. Using default configuration.", e.getMessage());
        }
    }

    /**
     * 打印已加载的 AI 服务配置
     */
    private void printLoadedAIServices(Map<String, Object> envMap) {
        String[] providers = {"OPENAI", "ANTHROPIC", "QWEN", "ZHIPU", "CUSTOM"};
        
        log.info("=== AI Services Configuration ===");
        for (String provider : providers) {
            String apiKey = (String) envMap.get(provider + "_API_KEY");
            String models = (String) envMap.get(provider + "_MODELS");
            
            if (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.contains("your-")) {
                String maskedKey = maskApiKey(apiKey);
                log.info("  {} - API Key: {}, Models: {}", 
                        provider, 
                        maskedKey, 
                        models != null ? models : "default");
            }
        }
        log.info("==================================");
    }

    /**
     * 隐藏 API 密钥的部分内容
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }


}
