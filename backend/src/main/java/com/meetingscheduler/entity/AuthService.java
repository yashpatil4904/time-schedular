package com.meetingscheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthService {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type")
    private AuthType authType;
    
    @Column(name = "performed_on")
    private LocalDateTime performedOn;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "token")
    private String token;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Relationship with User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        performedOn = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }
    
    public enum AuthType {
        JWT, OAUTH, BASIC_AUTH
    }
}
