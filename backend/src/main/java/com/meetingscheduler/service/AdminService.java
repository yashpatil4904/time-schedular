package com.meetingscheduler.service;

import com.meetingscheduler.entity.Meeting;
import com.meetingscheduler.entity.User;
import com.meetingscheduler.repository.MeetingRepository;
import com.meetingscheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    
    public List<User> getAllUsers() {
        log.info("Fetching all users for admin");
        return userRepository.findAll();
    }
    
    @Transactional
    public User updateUserRole(UUID userId, User.UserRole newRole) {
        log.info("Updating user {} role to {}", userId, newRole);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        
        log.info("User role updated successfully");
        return updatedUser;
    }
    
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Deleting user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Prevent deleting admin users
        if (user.getRole() == User.UserRole.admin) {
            throw new IllegalArgumentException("Cannot delete admin users");
        }
        
        userRepository.delete(user);
        log.info("User deleted successfully");
    }
    
    public Map<String, Object> getSystemStatistics() {
        log.info("Fetching system statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // User statistics
        List<User> allUsers = userRepository.findAll();
        stats.put("totalUsers", allUsers.size());
        stats.put("activeExecutives", userRepository.findByRole(User.UserRole.executive).size());
        stats.put("activeSecretaries", userRepository.findByRole(User.UserRole.secretary).size());
        stats.put("activeAdmins", userRepository.findByRole(User.UserRole.admin).size());
        
        // Meeting statistics
        List<Meeting> allMeetings = meetingRepository.findAll();
        stats.put("totalMeetings", allMeetings.size());
        stats.put("completedMeetings", meetingRepository.findByStatus(Meeting.MeetingStatus.completed).size());
        stats.put("pendingMeetings", meetingRepository.findByStatus(Meeting.MeetingStatus.pending).size());
        stats.put("scheduledMeetings", meetingRepository.findByStatus(Meeting.MeetingStatus.scheduled).size());
        
        // Calculate average optimization score (placeholder - would need Schedule repository)
        stats.put("averageOptimizationScore", 0.85);
        
        log.info("System statistics calculated: {}", stats);
        return stats;
    }
}

