package com.example.aichatsystem.controller;

import com.example.aichatsystem.dto.AIRoleDTO;
import com.example.aichatsystem.entity.User;
import com.example.aichatsystem.repository.UserRepository;
import com.example.aichatsystem.service.AIRoleService;
import com.example.aichatsystem.util.ResultUtil;
import com.example.aichatsystem.vo.AIRoleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class AIRoleController {

    private final AIRoleService aiRoleService;
    private final UserRepository userRepository;

    @PostMapping
    public ResultUtil<AIRoleVO> createRole(
            @Valid @RequestBody AIRoleDTO roleDTO,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        AIRoleVO role = aiRoleService.createRole(roleDTO, currentUser);
        return ResultUtil.success("Role created", role);
    }

    @GetMapping("/mine")
    public ResultUtil<List<AIRoleVO>> getUserRoles(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<AIRoleVO> roles = aiRoleService.getUserRoles(currentUser);
        return ResultUtil.success(roles);
    }

    @GetMapping("/public")
    public ResultUtil<List<AIRoleVO>> getPublicRoles() {
        List<AIRoleVO> roles = aiRoleService.getPublicRoles();
        return ResultUtil.success(roles);
    }

    @PutMapping("/{id}")
    public ResultUtil<AIRoleVO> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody AIRoleDTO roleDTO,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        AIRoleVO role = aiRoleService.updateRole(id, roleDTO, currentUser);
        return ResultUtil.success("Role updated", role);
    }

    @DeleteMapping("/{id}")
    public ResultUtil<Void> deleteRole(
            @PathVariable Long id,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        aiRoleService.deleteRole(id, currentUser);
        return ResultUtil.success("Role deleted", null);
    }

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
