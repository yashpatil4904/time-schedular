package com.meetingscheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@Entity
@Table(name = "participations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Participation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private ParticipationRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "response_status")
    private ResponseStatus responseStatus;
    
    // Relationship with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"meetings", "availabilities", "notifications", "password"})
    private User user;
    
    // Relationship with Meeting
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    @JsonIgnoreProperties({"user"})
    private Meeting meeting;
    
    public enum ParticipationRole {
        ORGANIZER, ATTENDEE, MODERATOR
    }
    
    public enum ResponseStatus {
        ACCEPTED, DECLINED, TENTATIVE, NO_RESPONSE
    }
}
