package com.example.aichatsystem.repository;

import com.example.aichatsystem.entity.AIRole;
import com.example.aichatsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIRoleRepository extends JpaRepository<AIRole, Long> {
    List<AIRole> findByUserAndEnabled(User user, Boolean enabled);
    List<AIRole> findByIsPublicTrueAndEnabled(Boolean enabled);
    Optional<AIRole> findByIdAndUser(Long id, User user);
    Long countByUser(User user);
}
