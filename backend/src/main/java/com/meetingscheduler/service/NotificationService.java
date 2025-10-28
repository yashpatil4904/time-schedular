package com.meetingscheduler.service;

import com.meetingscheduler.entity.Notification;
import com.meetingscheduler.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    public List<Notification> getNotificationsByUser(UUID userId) {
        log.info("Fetching notifications for user ID: {}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<Notification> getUnreadNotifications(UUID userId) {
        log.info("Fetching unread notifications for user ID: {}", userId);
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
    }
    
    public Long getUnreadNotificationCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }
    
    @Transactional
    public Notification markAsRead(UUID notificationId, UUID userId) {
        log.info("Marking notification as read: {}", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + notificationId));
        
        // Check if user owns this notification
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only mark your own notifications as read");
        }
        
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }
    
    @Transactional
    public void markAllAsRead(UUID userId) {
        log.info("Marking all notifications as read for user ID: {}", userId);
        
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }
    
    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        log.info("Deleting notification: {} for user: {}", notificationId, userId);
        
        try {
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + notificationId));
            
            // Check if user owns this notification
            if (notification.getUser() != null && !notification.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("You can only delete your own notifications");
            }
            
            notificationRepository.delete(notification);
            log.info("Notification deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting notification: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to delete notification: " + e.getMessage());
        }
    }
}