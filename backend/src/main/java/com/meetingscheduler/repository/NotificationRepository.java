package com.meetingscheduler.repository;

import com.meetingscheduler.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(UUID userId, Boolean isRead);
    
    Long countByUserIdAndIsRead(UUID userId, Boolean isRead);
}