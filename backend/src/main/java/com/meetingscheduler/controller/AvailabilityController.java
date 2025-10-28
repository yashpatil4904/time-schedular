package com.meetingscheduler.controller;

import com.meetingscheduler.dto.CreateAvailabilityRequest;
import com.meetingscheduler.entity.Availability;
import com.meetingscheduler.entity.User;
import com.meetingscheduler.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AvailabilityController {
    
    private final AvailabilityService availabilityService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Availability>> getAvailabilityByUser(@PathVariable UUID userId) {
        log.info("Fetching availability for user ID: {}", userId);
        List<Availability> availabilities = availabilityService.getAvailabilityByUser(userId);
        return ResponseEntity.ok(availabilities);
    }
    
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> createAvailability(@PathVariable UUID userId, 
                                              @Valid @RequestBody CreateAvailabilityRequest request) {
        log.info("Creating availability for user ID: {}", userId);
        
        try {
            Availability availability = new Availability();
            availability.setStartTime(request.getStartTime());
            availability.setEndTime(request.getEndTime());
            
            // Create a temporary user object with just the ID
            User user = new User();
            user.setId(userId);
            
            Availability createdAvailability = availabilityService.createAvailability(availability, user);
            return ResponseEntity.ok(createdAvailability);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating availability: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to create availability: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{availabilityId}/user/{userId}")
    public ResponseEntity<?> deleteAvailability(@PathVariable UUID availabilityId, 
                                              @PathVariable UUID userId) {
        log.info("Deleting availability ID: {} for user ID: {}", availabilityId, userId);
        
        try {
            availabilityService.deleteAvailability(availabilityId, userId);
            return ResponseEntity.ok("Availability deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting availability: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to delete availability: " + e.getMessage());
        }
    }
}

