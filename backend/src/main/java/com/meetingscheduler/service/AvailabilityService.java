package com.meetingscheduler.service;

import com.meetingscheduler.entity.Availability;
import com.meetingscheduler.entity.User;
import com.meetingscheduler.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {
    
    private final AvailabilityRepository availabilityRepository;
    
    public List<Availability> getAvailabilityByUser(UUID userId) {
        log.info("Fetching availability for user ID: {}", userId);
        return availabilityRepository.findByUserIdOrderByStartTimeAsc(userId);
    }
    
    @Transactional
    public Availability createAvailability(Availability availability, User user) {
        log.info("Creating availability for user ID: {}", user.getId());
        
        // Validate availability
        validateAvailability(availability);
        
        availability.setUser(user);
        return availabilityRepository.save(availability);
    }
    
    @Transactional
    public void deleteAvailability(UUID availabilityId, UUID userId) {
        log.info("Deleting availability with ID: {}", availabilityId);
        
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new IllegalArgumentException("Availability not found with ID: " + availabilityId));
        
        // Check if user owns this availability
        if (!availability.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own availability");
        }
        
        availabilityRepository.delete(availability);
    }
    
    private void validateAvailability(Availability availability) {
        if (availability.getStartTime() == null) {
            throw new IllegalArgumentException("Start time is required");
        }
        
        if (availability.getEndTime() == null) {
            throw new IllegalArgumentException("End time is required");
        }
        
        if (availability.getEndTime().isBefore(availability.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        if (availability.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time cannot be in the past");
        }
    }
}

