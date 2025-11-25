package com.example.aichatsystem.service;

import com.example.aichatsystem.dto.ChangePasswordDTO;
import com.example.aichatsystem.dto.UpdateProfileDTO;
import com.example.aichatsystem.dto.UserLoginDTO;
import com.example.aichatsystem.dto.UserRegisterDTO;
import com.example.aichatsystem.entity.User;
import com.example.aichatsystem.entity.VerificationToken;
import com.example.aichatsystem.exception.BusinessException;
import com.example.aichatsystem.repository.UserRepository;
import com.example.aichatsystem.repository.VerificationTokenRepository;
import com.example.aichatsystem.security.JwtUtil;
import com.example.aichatsystem.vo.LoginVO;
import com.example.aichatsystem.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final VerificationTokenRepository tokenRepository;

    @Value("${spring.security.jwt.expiration}")
    private Long jwtExpiration;

    @Transactional
    public UserVO register(UserRegisterDTO registerDTO) {
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new BusinessException("Username already exists");
        }

        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setEnabled(true);
        user.setEmailVerified(true); // 注册验证码已验证，直接设置为已验证

        User savedUser = userRepository.save(user);
        
        return convertToUserVO(savedUser);
    }

    public LoginVO login(UserLoginDTO loginDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                )
        );

        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        String token = jwtUtil.generateToken(user.getUsername());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(convertToUserVO(user));
        loginVO.setExpiresIn(jwtExpiration);

        return loginVO;
    }

    private UserVO convertToUserVO(User user) {
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setEmail(user.getEmail());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setCreatedAt(user.getCreatedAt());
        return userVO;
    }
    
    public UserVO getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));
        return convertToUserVO(user);
    }
    
    @Transactional
    public UserVO updateProfile(String username, UpdateProfileDTO updateDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDTO.getEmail())) {
                throw new BusinessException("Email already exists");
            }
            user.setEmail(updateDTO.getEmail());
            user.setEmailVerified(true); // 更新邮箱时直接设置为已验证
        }
        
        if (updateDTO.getAvatar() != null) {
            user.setAvatar(updateDTO.getAvatar());
        }
        
        User updatedUser = userRepository.save(user);
        return convertToUserVO(updatedUser);
    }
    
    @Transactional
    public void changePassword(String username, ChangePasswordDTO changePasswordDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }
        
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }
        
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
    }
    
    /**
     * 发送注册验证码
     */
    @Transactional
    public void sendRegistrationVerificationCode(String email) {
        // 检查邮箱是否已被真实用户注册（排除临时用户）
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getEnabled()) {
                throw new BusinessException("Email already registered");
            }
        });
        
        try {
            // 生成6位数字验证码
            Random random = new Random();
            String code = String.format("%06d", random.nextInt(1000000));
            
            // 删除该邮箱的旧验证码
            List<VerificationToken> oldTokens = tokenRepository.findByEmailAndTokenType(email, "REGISTRATION_CODE");
            if (!oldTokens.isEmpty()) {
                tokenRepository.deleteAll(oldTokens);
            }
            
            // 保存验证码（10分钟有效）
            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setToken(code);
            verificationToken.setEmail(email);
            verificationToken.setUser(null);
            verificationToken.setTokenType("REGISTRATION_CODE");
            verificationToken.setExpiryDate(java.time.LocalDateTime.now().plusMinutes(10));
            verificationToken.setUsed(false);
            tokenRepository.save(verificationToken);
            
            // 发送邮件
            emailService.sendRegistrationVerificationCode(email, code);
            log.info("Registration verification code sent to: {}", email);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to send registration verification code: {}", e.getMessage());
            throw new BusinessException("Failed to send verification code");
        }
    }
    
    /**
     * 验证注册验证码
     */
    @Transactional
    public void verifyRegistrationCode(String email, String code) {
        // 查找验证码
        VerificationToken token = tokenRepository.findByEmailAndTokenAndTokenType(email, code, "REGISTRATION_CODE")
                .orElseThrow(() -> new BusinessException("Invalid verification code"));
        
        if (token.isExpired()) {
            throw new BusinessException("Verification code has expired");
        }
        
        if (token.getUsed()) {
            throw new BusinessException("Verification code has already been used");
        }
        
        // 标记验证码为已使用
        token.setUsed(true);
        tokenRepository.save(token);
    }
}
