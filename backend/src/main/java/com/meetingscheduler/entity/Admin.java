package com.meetingscheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "admins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "id")
public class Admin extends User {
    
    @Column(name = "admin_id", unique = true, nullable = false)
    private UUID adminId;
    
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        setRole(UserRole.admin);
    }
}
