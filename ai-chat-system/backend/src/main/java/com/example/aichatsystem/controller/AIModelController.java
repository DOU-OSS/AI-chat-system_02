package com.example.aichatsystem.controller;

import com.example.aichatsystem.dto.AIModelDTO;
import com.example.aichatsystem.service.AIModelService;
import com.example.aichatsystem.util.ResultUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai-models")
@RequiredArgsConstructor
@Slf4j
public class AIModelController {
    
    private final AIModelService aiModelService;
    
    /**
     * 获取所有可用的AI模型
     */
    @GetMapping
    public ResultUtil<List<AIModelDTO>> getAvailableModels() {
        log.info("Getting available AI models");
        List<AIModelDTO> models = aiModelService.getAvailableModels();
        return ResultUtil.success(models);
    }
}
