package com.meetingscheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "secretaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "id")
public class Secretary extends User {
    
    @Column(name = "employee_id", unique = true, nullable = false)
    private UUID employeeId;
    
    @OneToMany(mappedBy = "secretary", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Assists> assistsList;
    
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        setRole(UserRole.secretary);
    }
}
