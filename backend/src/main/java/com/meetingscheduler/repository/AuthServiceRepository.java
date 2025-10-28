package com.meetingscheduler.repository;

import com.meetingscheduler.entity.AuthService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthServiceRepository extends JpaRepository<AuthService, UUID> {
    
    Optional<AuthService> findByUserIdAndIsActiveTrue(UUID userId);
    
    Optional<AuthService> findByTokenAndIsActiveTrue(String token);
    
    void deleteByUserIdAndIsActiveFalse(UUID userId);
}
