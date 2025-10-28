package com.meetingscheduler.controller;

import com.meetingscheduler.algorithm.ScheduleOptimizer;
import com.meetingscheduler.entity.Schedule;
import com.meetingscheduler.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ScheduleController {
    
    private final ScheduleService scheduleService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Schedule>> getSchedulesByUser(@PathVariable UUID userId) {
        log.info("Fetching schedules for user ID: {}", userId);
        List<Schedule> schedules = scheduleService.getSchedulesByUser(userId);
        return ResponseEntity.ok(schedules);
    }
    
    @PostMapping("/optimize/user/{userId}")
    public ResponseEntity<?> optimizeSchedule(@PathVariable UUID userId) {
        log.info("Optimizing schedule for user ID: {}", userId);
        
        try {
            ScheduleOptimizer.OptimizedScheduleResult result = scheduleService.optimizeSchedule(userId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error optimizing schedule: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to optimize schedule: " + e.getMessage());
        }
    }
    
    @PostMapping("/custom/user/{userId}")
    public ResponseEntity<?> createCustomSchedule(@PathVariable UUID userId, 
                                                @RequestBody CustomScheduleRequest request) {
        log.info("Creating custom schedule for user ID: {}", userId);
        
        try {
            Schedule schedule = scheduleService.createCustomSchedule(userId, request.getMeetingId(), 
                request.getStartTime(), request.getEndTime());
            return ResponseEntity.ok(schedule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating custom schedule: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to create custom schedule: " + e.getMessage());
        }
    }
    
    @PutMapping("/update/user/{userId}")
    public ResponseEntity<?> updateMeetingSchedule(@PathVariable UUID userId, 
                                                 @RequestBody UpdateScheduleRequest request) {
        log.info("Updating meeting schedule for user ID: {}", userId);
        
        try {
            Schedule schedule = scheduleService.updateMeetingSchedule(userId, request.getMeetingId(), 
                request.getStartTime(), request.getEndTime());
            return ResponseEntity.ok(schedule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating meeting schedule: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to update meeting schedule: " + e.getMessage());
        }
    }
    
    // DTO for custom schedule request
    public static class CustomScheduleRequest {
        private UUID meetingId;
        private String startTime;
        private String endTime;
        
        // Getters and setters
        public UUID getMeetingId() { return meetingId; }
        public void setMeetingId(UUID meetingId) { this.meetingId = meetingId; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
    }
    
    // DTO for update schedule request
    public static class UpdateScheduleRequest {
        private UUID meetingId;
        private String startTime;
        private String endTime;
        
        // Getters and setters
        public UUID getMeetingId() { return meetingId; }
        public void setMeetingId(UUID meetingId) { this.meetingId = meetingId; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
    }
}