package com.example.aichatsystem.service;

import com.example.aichatsystem.entity.*;
import com.example.aichatsystem.exception.BusinessException;
import com.example.aichatsystem.repository.*;
import com.example.aichatsystem.vo.ConversationVO;
import com.example.aichatsystem.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AIRoleRepository aiRoleRepository;

    @Transactional
    public ConversationVO createConversation(String title, Long aiRoleId, String selectedModel, User currentUser) {
        Conversation conversation = new Conversation();
        conversation.setTitle(title != null ? title : "New Conversation");
        conversation.setUser(currentUser);
        conversation.setActive(true);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setSelectedModel(selectedModel); // 保存用户选择的模型

        if (aiRoleId != null) {
            AIRole aiRole = aiRoleRepository.findById(aiRoleId)
                    .orElseThrow(() -> new BusinessException("AI Role not found"));
            conversation.setAiRole(aiRole);
        }

        Conversation saved = conversationRepository.save(conversation);
        return convertToConversationVO(saved);
    }

    public List<ConversationVO> getUserConversations(User currentUser) {
        // 只返回未删除且活跃的会话
        List<Conversation> conversations = conversationRepository
                .findByUserAndActiveOrderByLastMessageAtDesc(currentUser, true)
                .stream()
                .filter(c -> !c.getIsDeleted())
                .collect(Collectors.toList());
        
        return conversations.stream()
                .map(this::convertToConversationVO)
                .collect(Collectors.toList());
    }
    
    public List<ConversationVO> getDeletedConversations(User currentUser) {
        List<Conversation> conversations = conversationRepository
                .findByUserAndIsDeletedOrderByDeletedAtDesc(currentUser, true);
        
        return conversations.stream()
                .map(this::convertToConversationVO)
                .collect(Collectors.toList());
    }

    public ConversationVO getConversation(Long conversationId, User currentUser) {
        Conversation conversation = conversationRepository
                .findByIdAndUser(conversationId, currentUser)
                .orElseThrow(() -> new BusinessException("Conversation not found"));
        
        ConversationVO vo = convertToConversationVO(conversation);
        
        // Load messages
        List<Message> messages = messageRepository.findByConversationOrderByCreatedAtAsc(conversation);
        vo.setMessages(messages.stream()
                .map(this::convertToMessageVO)
                .collect(Collectors.toList()));
        
        return vo;
    }

    @Transactional
    public void deleteConversation(Long conversationId, User currentUser, boolean permanent) {
        log.info("Deleting conversation {} for user {} (permanent: {})", 
                conversationId, currentUser.getUsername(), permanent);
        
        Conversation conversation = conversationRepository
                .findByIdAndUser(conversationId, currentUser)
                .orElseThrow(() -> new BusinessException("Conversation not found"));
        
        if (permanent || conversation.getIsDeleted()) {
            // 永久删除：从数据库中彻底删除
            log.info("Permanently deleting conversation {}", conversationId);
            conversationRepository.delete(conversation);
            // 同时删除关联的消息
            messageRepository.deleteAll(conversation.getMessages());
        } else {
            // 软删除：移到回收站
            log.info("Moving conversation {} to recycle bin", conversationId);
            conversation.setIsDeleted(true);
            conversation.setDeletedAt(LocalDateTime.now());
            conversation.setActive(false);
            conversationRepository.save(conversation);
        }
    }
    
    @Transactional
    public void restoreConversation(Long conversationId, User currentUser) {
        log.info("Restoring conversation {} for user {}", conversationId, currentUser.getUsername());
        
        Conversation conversation = conversationRepository
                .findByIdAndUser(conversationId, currentUser)
                .orElseThrow(() -> new BusinessException("Conversation not found"));
        
        if (!conversation.getIsDeleted()) {
            throw new BusinessException("Conversation is not in recycle bin");
        }
        
        conversation.setIsDeleted(false);
        conversation.setDeletedAt(null);
        conversation.setActive(true);
        conversationRepository.save(conversation);
        
        log.info("Conversation {} restored successfully", conversationId);
    }
    
    @Transactional
    public void emptyRecycleBin(User currentUser) {
        log.info("Emptying recycle bin for user {}", currentUser.getUsername());
        
        List<Conversation> deletedConversations = conversationRepository
                .findByUserAndIsDeletedOrderByDeletedAtDesc(currentUser, true);
        
        for (Conversation conversation : deletedConversations) {
            // 删除关联的消息
            messageRepository.deleteAll(conversation.getMessages());
            // 删除会话
            conversationRepository.delete(conversation);
        }
        
        log.info("Deleted {} conversations from recycle bin", deletedConversations.size());
    }

    @Transactional
    public ConversationVO updateConversationModel(Long conversationId, String selectedModel, User currentUser) {
        Conversation conversation = conversationRepository
                .findByIdAndUser(conversationId, currentUser)
                .orElseThrow(() -> new BusinessException("Conversation not found"));
        
        conversation.setSelectedModel(selectedModel);
        Conversation saved = conversationRepository.save(conversation);
        return convertToConversationVO(saved);
    }

    private ConversationVO convertToConversationVO(Conversation conversation) {
        ConversationVO vo = new ConversationVO();
        vo.setId(conversation.getId());
        vo.setTitle(conversation.getTitle());
        if (conversation.getAiRole() != null) {
            vo.setAiRoleId(conversation.getAiRole().getId());
            vo.setAiRoleName(conversation.getAiRole().getName());
            vo.setAiRoleDescription(conversation.getAiRole().getDescription());
        }
        vo.setSelectedModel(conversation.getSelectedModel()); // 包含选择的模型
        vo.setCreatedAt(conversation.getCreatedAt());
        vo.setLastMessageAt(conversation.getLastMessageAt());
        vo.setDeletedAt(conversation.getDeletedAt());
        return vo;
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
}
