package com.example.aichatsystem.controller;

import com.example.aichatsystem.dto.ChangePasswordDTO;
import com.example.aichatsystem.dto.UpdateProfileDTO;
import com.example.aichatsystem.dto.UserLoginDTO;
import com.example.aichatsystem.dto.UserRegisterDTO;
import com.example.aichatsystem.service.AuthService;
import com.example.aichatsystem.util.ResultUtil;
import com.example.aichatsystem.vo.LoginVO;
import com.example.aichatsystem.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResultUtil<UserVO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        UserVO user = authService.register(registerDTO);
        return ResultUtil.success("Registration successful", user);
    }

    @PostMapping("/login")
    public ResultUtil<LoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return ResultUtil.success("Login successful", loginVO);
    }
    
    @GetMapping("/profile")
    public ResultUtil<UserVO> getProfile(Authentication authentication) {
        UserVO user = authService.getProfile(authentication.getName());
        return ResultUtil.success("Profile fetched successfully", user);
    }
    
    @PutMapping("/profile")
    public ResultUtil<UserVO> updateProfile(
            @Valid @RequestBody UpdateProfileDTO updateDTO,
            Authentication authentication) {
        UserVO user = authService.updateProfile(authentication.getName(), updateDTO);
        return ResultUtil.success("Profile updated successfully", user);
    }
    
    @PostMapping("/change-password")
    public ResultUtil<String> changePassword(
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO,
            Authentication authentication) {
        authService.changePassword(authentication.getName(), changePasswordDTO);
        return ResultUtil.success("Password changed successfully");
    }
    
    @PostMapping("/send-verification-code")
    public ResultUtil<String> sendVerificationCode(@RequestParam String email) {
        authService.sendRegistrationVerificationCode(email);
        return ResultUtil.success("Verification code sent");
    }
    
    @PostMapping("/verify-code")
    public ResultUtil<String> verifyCode(@RequestParam String email, @RequestParam String code) {
        authService.verifyRegistrationCode(email, code);
        return ResultUtil.success("Verification code is valid");
    }
}
