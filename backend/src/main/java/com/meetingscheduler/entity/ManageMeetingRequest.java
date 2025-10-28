package com.meetingscheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "manage_meeting_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManageMeetingRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "requester_id")
    private UUID requesterId;
    
    @Column(name = "executive_approval")
    private Boolean executiveApproval;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Relationship with User (Executive who made the request)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", insertable = false, updatable = false)
    private Executive requester;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        timestamp = LocalDateTime.now();
        if (executiveApproval == null) {
            executiveApproval = false;
        }
    }
}
