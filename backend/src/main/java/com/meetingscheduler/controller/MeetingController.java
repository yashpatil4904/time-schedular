package com.meetingscheduler.controller;

import com.meetingscheduler.dto.CreateMeetingRequest;
import com.meetingscheduler.dto.CreateMeetingWithParticipantsRequest;
import com.meetingscheduler.entity.Meeting;
import com.meetingscheduler.entity.User;
import com.meetingscheduler.service.MeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MeetingController {
    
    private final MeetingService meetingService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Meeting>> getMeetingsByUser(@PathVariable UUID userId) {
        log.info("Fetching meetings for user ID: {}", userId);
        List<Meeting> meetings = meetingService.getMeetingsByUser(userId);
        return ResponseEntity.ok(meetings);
    }
    
    @GetMapping("/pending/user/{userId}")
    public ResponseEntity<List<Meeting>> getPendingMeetings(@PathVariable UUID userId) {
        log.info("Fetching pending meetings for user ID: {}", userId);
        List<Meeting> meetings = meetingService.getPendingMeetings(userId);
        return ResponseEntity.ok(meetings);
    }
    
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> createMeeting(@PathVariable UUID userId, 
                                         @Valid @RequestBody CreateMeetingRequest request) {
        log.info("Creating meeting for user ID: {}", userId);
        
        try {
            Meeting meeting = new Meeting();
            meeting.setTitle(request.getTitle());
            meeting.setDescription(request.getDescription());
            meeting.setPriority(request.getPriority());
            meeting.setDurationMinutes(request.getDurationMinutes());
            meeting.setDeadline(request.getDeadline());
            
            // Create a temporary user object with just the ID
            User user = new User();
            user.setId(userId);
            
            Meeting createdMeeting = meetingService.createMeeting(meeting, user);
            return ResponseEntity.ok(createdMeeting);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating meeting: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to create meeting: " + e.getMessage());
        }
    }
    
    @PutMapping("/{meetingId}/user/{userId}")
    public ResponseEntity<?> updateMeeting(@PathVariable UUID meetingId, 
                                         @PathVariable UUID userId,
                                         @Valid @RequestBody CreateMeetingRequest request) {
        log.info("Updating meeting ID: {} for user ID: {}", meetingId, userId);
        
        try {
            Meeting meetingDetails = new Meeting();
            meetingDetails.setTitle(request.getTitle());
            meetingDetails.setDescription(request.getDescription());
            meetingDetails.setPriority(request.getPriority());
            meetingDetails.setDurationMinutes(request.getDurationMinutes());
            meetingDetails.setDeadline(request.getDeadline());
            
            Meeting updatedMeeting = meetingService.updateMeeting(meetingId, meetingDetails, userId);
            return ResponseEntity.ok(updatedMeeting);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating meeting: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to update meeting: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{meetingId}/user/{userId}")
    public ResponseEntity<?> deleteMeeting(@PathVariable UUID meetingId, 
                                         @PathVariable UUID userId) {
        log.info("Deleting meeting ID: {} for user ID: {}", meetingId, userId);
        
        try {
            meetingService.deleteMeeting(meetingId, userId);
            return ResponseEntity.ok("Meeting deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting meeting: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to delete meeting: " + e.getMessage());
        }
    }
    
    /**
     * ✨ NEW: Mark meeting as completed
     * Endpoint: POST /api/meetings/{meetingId}/complete/user/{userId}
     */
    @PostMapping("/{meetingId}/complete/user/{userId}")
    public ResponseEntity<?> markMeetingAsCompleted(@PathVariable UUID meetingId, 
                                                    @PathVariable UUID userId) {
        log.info("Marking meeting ID: {} as completed for user ID: {}", meetingId, userId);
        
        try {
            Meeting completedMeeting = meetingService.markMeetingAsCompleted(meetingId, userId);
            return ResponseEntity.ok(completedMeeting);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error marking meeting as completed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to mark meeting as completed: " + e.getMessage());
        }
    }
    
    /**
     * ✨ NEW: Create meeting with participants
     * Endpoint: POST /api/meetings/with-participants/user/{userId}
     */
    @PostMapping("/with-participants/user/{userId}")
    public ResponseEntity<?> createMeetingWithParticipants(@PathVariable UUID userId, 
                                                          @Valid @RequestBody CreateMeetingWithParticipantsRequest request) {
        log.info("Creating meeting with participants for user ID: {}", userId);
        
        try {
            Meeting meeting = new Meeting();
            meeting.setTitle(request.getTitle());
            meeting.setDescription(request.getDescription());
            meeting.setPriority(request.getPriority());
            meeting.setDurationMinutes(request.getDurationMinutes());
            meeting.setDeadline(request.getDeadline());
            
            User organizer = new User();
            organizer.setId(userId);
            
            Meeting createdMeeting = meetingService.createMeetingWithParticipants(
                meeting, organizer, request.getParticipantIds());
            return ResponseEntity.ok(createdMeeting);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating meeting with participants: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to create meeting: " + e.getMessage());
        }
    }
}