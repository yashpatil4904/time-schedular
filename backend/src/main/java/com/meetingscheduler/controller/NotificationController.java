package com.meetingscheduler.controller;

import com.meetingscheduler.entity.Notification;
import com.meetingscheduler.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable UUID userId) {
        log.info("Fetching notifications for user ID: {}", userId);
        List<Notification> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/unread/user/{userId}")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable UUID userId) {
        log.info("Fetching unread notifications for user ID: {}", userId);
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/count/unread/user/{userId}")
    public ResponseEntity<Long> getUnreadNotificationCount(@PathVariable UUID userId) {
        log.info("Fetching unread notification count for user ID: {}", userId);
        Long count = notificationService.getUnreadNotificationCount(userId);
        return ResponseEntity.ok(count);
    }
    
    @PostMapping("/{notificationId}/mark-read/user/{userId}")
    public ResponseEntity<?> markAsRead(@PathVariable UUID notificationId, 
                                      @PathVariable UUID userId) {
        log.info("Marking notification as read: {} for user: {}", notificationId, userId);
        
        try {
            Notification notification = notificationService.markAsRead(notificationId, userId);
            return ResponseEntity.ok(notification);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to mark notification as read: " + e.getMessage());
        }
    }
    
    @PostMapping("/mark-all-read/user/{userId}")
    public ResponseEntity<?> markAllAsRead(@PathVariable UUID userId) {
        log.info("Marking all notifications as read for user ID: {}", userId);
        
        try {
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok("All notifications marked as read");
        } catch (Exception e) {
            log.error("Error marking all notifications as read: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to mark all notifications as read: " + e.getMessage());
        }
    }
}

