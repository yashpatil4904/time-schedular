package com.meetingscheduler.dto;

import com.meetingscheduler.entity.User;
import lombok.Data;

import java.util.UUID;

@Data
public class AuthResponse {
    
    private UUID userId;
    private String email;
    private String fullName;
    private User.UserRole role;
    private String message;
}