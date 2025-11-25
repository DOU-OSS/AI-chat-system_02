package com.example.aichatsystem.config;

import com.example.aichatsystem.entity.AIRole;
import com.example.aichatsystem.entity.User;
import com.example.aichatsystem.repository.AIRoleRepository;
import com.example.aichatsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final AIRoleRepository aiRoleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        initializeDefaultTestAccounts();
        initializeDefaultAIRoles();
    }
    
    private void initializeDefaultTestAccounts() {
        // 检查是否已有测试账号
        if (userRepository.findByUsername("demo").isPresent()) {
            log.info("Test accounts already initialized.");
            return;
        }
        
        log.info("Initializing default test accounts...");
        
        // 创建demo账号
        User demoUser = new User();
        demoUser.setUsername("demo");
        demoUser.setPassword(passwordEncoder.encode("demo123456"));
        demoUser.setEmail("demo@ai-chat.com");
        demoUser.setNickname("Demo User");
        demoUser.setEmailVerified(true);  // 测试账号默认已验证
        userRepository.save(demoUser);
        
        log.info("Successfully initialized test account: demo/demo123456");
    }
    
    private void initializeDefaultAIRoles() {
        // 检查是否已有默认角色
        long count = aiRoleRepository.count();
        if (count > 0) {
            log.info("AI roles already initialized. Found {} roles.", count);
            return;
        }
        
        log.info("Initializing default AI roles...");
        
        List<AIRole> defaultRoles = Arrays.asList(
            createRole("通用助手", "我是一个友好且知识渊博的AI助手，可以帮助您解答各种问题。",
                "你是一个友好、专业的AI助手。请用清晰、准确的语言回答用户的问题。",
                "assistant", true),
            
            createRole("程序员", "我是一位经验丰富的程序员，精通多种编程语言和技术栈。",
                "你是一位资深程序员，精通Java、Python、JavaScript等语言。请提供专业的编程建议和代码示例。",
                "programmer", false),
            
            createRole("英语老师", "我是您的英语老师，可以帮助您学习英语语法、词汇和口语表达。",
                "你是一位耐心的英语老师。请用简单易懂的方式解释英语知识，并提供例句帮助理解。",
                "teacher", false),
            
            createRole("创意写手", "我是一位富有创意的写手，可以帮您撰写各种文案和创意内容。",
                "你是一位创意写手。请发挥想象力，创作有趣且吸引人的内容。",
                "writer", false),
            
            createRole("心理咨询师", "我可以倾听您的烦恼，提供情感支持和心理建议。",
                "你是一位富有同理心的心理咨询师。请用温暖、理解的语气与用户交流，提供情感支持。",
                "counselor", false),
            
            createRole("数据分析师", "我是专业的数据分析师，可以帮您分析数据并提供洞察。",
                "你是一位数据分析专家。请用数据驱动的方式分析问题，提供清晰的见解和建议。",
                "analyst", false)
        );
        
        aiRoleRepository.saveAll(defaultRoles);
        log.info("Successfully initialized {} AI roles.", defaultRoles.size());
    }
    
    private AIRole createRole(String name, String description, String systemPrompt, String icon, boolean isDefault) {
        AIRole role = new AIRole();
        role.setName(name);
        role.setDescription(description);
        role.setSystemPrompt(systemPrompt);
        role.setIcon(icon);
        role.setIsDefault(isDefault);
        role.setIsSystem(true);
        role.setIsPublic(true);
        role.setEnabled(true);
        // System roles don't have a user, but we need to handle this
        // We'll need to make the user field nullable for system roles
        return role;
    }
}
