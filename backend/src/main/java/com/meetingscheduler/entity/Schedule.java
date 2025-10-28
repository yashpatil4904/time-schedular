package com.meetingscheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Schedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "scheduled_start", nullable = false)
    private LocalDateTime scheduledStart;
    
    @Column(name = "scheduled_end", nullable = false)
    private LocalDateTime scheduledEnd;
    
    @Column(name = "optimization_score")
    private Double optimizationScore;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Relationship with Meeting
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    @JsonIgnoreProperties({"user"})
    private Meeting meeting;
    
    // Relationship with User (single user system)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"meetings", "availabilities", "notifications", "password"})
    private User user;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}