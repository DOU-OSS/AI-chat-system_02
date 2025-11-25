package com.example.aichatsystem.repository;

import com.example.aichatsystem.entity.Conversation;
import com.example.aichatsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUserAndActiveOrderByLastMessageAtDesc(User user, Boolean active);
    List<Conversation> findByUserAndIsDeletedOrderByDeletedAtDesc(User user, Boolean isDeleted);
    Optional<Conversation> findByIdAndUser(Long id, User user);
    Long countByUser(User user);
    Long countByUserAndActive(User user, Boolean active);
    List<Conversation> findByUser(User user);
}
