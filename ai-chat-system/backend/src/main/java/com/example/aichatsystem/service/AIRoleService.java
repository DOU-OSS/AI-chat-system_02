package com.example.aichatsystem.service;

import com.example.aichatsystem.dto.AIRoleDTO;
import com.example.aichatsystem.entity.AIRole;
import com.example.aichatsystem.entity.User;
import com.example.aichatsystem.exception.BusinessException;
import com.example.aichatsystem.repository.AIRoleRepository;
import com.example.aichatsystem.vo.AIRoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIRoleService {

    private final AIRoleRepository aiRoleRepository;

    @Transactional
    public AIRoleVO createRole(AIRoleDTO roleDTO, User currentUser) {
        AIRole role = new AIRole();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        role.setSystemPrompt(roleDTO.getSystemPrompt());
        role.setModel(roleDTO.getModel());
        role.setIsPublic(roleDTO.getIsPublic());
        role.setUser(currentUser);
        role.setEnabled(true);

        AIRole saved = aiRoleRepository.save(role);
        return convertToAIRoleVO(saved);
    }

    public List<AIRoleVO> getUserRoles(User currentUser) {
        List<AIRole> roles = aiRoleRepository.findByUserAndEnabled(currentUser, true);
        return roles.stream()
                .map(this::convertToAIRoleVO)
                .collect(Collectors.toList());
    }

    public List<AIRoleVO> getPublicRoles() {
        List<AIRole> roles = aiRoleRepository.findByIsPublicTrueAndEnabled(true);
        return roles.stream()
                .map(this::convertToAIRoleVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AIRoleVO updateRole(Long roleId, AIRoleDTO roleDTO, User currentUser) {
        AIRole role = aiRoleRepository.findByIdAndUser(roleId, currentUser)
                .orElseThrow(() -> new BusinessException("Role not found or you don't have permission"));

        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        role.setSystemPrompt(roleDTO.getSystemPrompt());
        role.setModel(roleDTO.getModel());
        role.setIsPublic(roleDTO.getIsPublic());

        AIRole saved = aiRoleRepository.save(role);
        return convertToAIRoleVO(saved);
    }

    @Transactional
    public void deleteRole(Long roleId, User currentUser) {
        AIRole role = aiRoleRepository.findByIdAndUser(roleId, currentUser)
                .orElseThrow(() -> new BusinessException("Role not found or you don't have permission"));

        role.setEnabled(false);
        aiRoleRepository.save(role);
    }

    private AIRoleVO convertToAIRoleVO(AIRole role) {
        AIRoleVO vo = new AIRoleVO();
        vo.setId(role.getId());
        vo.setName(role.getName());
        vo.setDescription(role.getDescription());
        vo.setSystemPrompt(role.getSystemPrompt());
        vo.setModel(role.getModel());
        vo.setIsPublic(role.getIsPublic());
        vo.setCreatedAt(role.getCreatedAt());
        return vo;
    }
}
