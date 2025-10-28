package com.meetingscheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "calendar_integrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarIntegration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "calendar_type")
    private CalendarType calendarType;
    
    @Column(name = "auth_token")
    private String authToken;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }
    
    public enum CalendarType {
        GOOGLE_CALENDAR, OUTLOOK_CALENDAR, APPLE_CALENDAR
    }
}
