package com.meetingscheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assists {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "assignment_resp")
    private String assignmentResp;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "permissions")
    private Permissions permissions;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "assigner_id")
    private UUID assignerId;
    
    @Column(name = "assignee_id")
    private UUID assigneeId;
    
    // Relationship with Secretary
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", insertable = false, updatable = false)
    private Secretary secretary;
    
    // Relationship with Executive
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigner_id", insertable = false, updatable = false)
    private Executive executive;
    
    public enum Permissions {
        READ_ONLY, FULL_ACCESS, LIMITED_ACCESS
    }
}
