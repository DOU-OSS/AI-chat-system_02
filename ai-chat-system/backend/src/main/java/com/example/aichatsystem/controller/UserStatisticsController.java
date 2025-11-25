package com.example.aichatsystem.controller;

import com.example.aichatsystem.dto.UserStatisticsDTO;
import com.example.aichatsystem.entity.User;
import com.example.aichatsystem.exception.BusinessException;
import com.example.aichatsystem.repository.UserRepository;
import com.example.aichatsystem.service.UserStatisticsService;
import com.example.aichatsystem.util.ResultUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Slf4j
public class UserStatisticsController {
    
    private final UserStatisticsService userStatisticsService;
    private final UserRepository userRepository;
    
    /**
     * 获取当前用户的使用统计
     */
    @GetMapping
    public ResultUtil<UserStatisticsDTO> getUserStatistics(Authentication authentication) {
        String username = authentication.getName();
        log.info("Get statistics for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        UserStatisticsDTO statistics = userStatisticsService.getUserStatistics(user);
        return ResultUtil.success(statistics);
    }
}
