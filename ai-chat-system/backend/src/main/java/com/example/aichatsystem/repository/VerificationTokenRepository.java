package com.example.aichatsystem.repository;

import com.example.aichatsystem.entity.VerificationToken;
import com.example.aichatsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    
    Optional<VerificationToken> findByTokenAndTokenType(String token, String tokenType);
    
    List<VerificationToken> findByUser(User user);
    
    List<VerificationToken> findByUserAndTokenType(User user, String tokenType);
    
    void deleteByExpiryDateBefore(LocalDateTime date);
    
    void deleteByUserAndTokenType(User user, String tokenType);
    
    Optional<VerificationToken> findByUserAndTokenAndTokenType(User user, String token, String tokenType);
    
    List<VerificationToken> findByEmailAndTokenType(String email, String tokenType);
    
    Optional<VerificationToken> findByEmailAndTokenAndTokenType(String email, String token, String tokenType);
}
