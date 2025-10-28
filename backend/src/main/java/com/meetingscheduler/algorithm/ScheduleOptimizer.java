package com.meetingscheduler.algorithm;

import com.meetingscheduler.entity.Meeting;
import com.meetingscheduler.entity.Availability;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class ScheduleOptimizer {
    
    // Weights for optimization algorithm
    private static final double PRIORITY_WEIGHT = 0.5;    // 50%
    private static final double DEADLINE_WEIGHT = 0.3;    // 30%
    private static final double DURATION_WEIGHT = 0.2;    // 20%
    
    public OptimizedScheduleResult optimizeSchedule(List<Meeting> meetings, List<Availability> availabilities) {
        System.out.println("=== OPTIMIZATION START ===");
        System.out.println("Meetings to schedule: " + meetings.size());
        System.out.println("Availability slots: " + availabilities.size());
        
        List<ScheduledMeeting> scheduledMeetings = new ArrayList<>();
        List<Meeting> remainingMeetings = new ArrayList<>(meetings);
        
        // Sort meetings by priority score (highest first) - GREEDY APPROACH
        remainingMeetings.sort((a, b) -> Double.compare(calculateMeetingScore(b), calculateMeetingScore(a)));
        
        System.out.println("Meetings sorted by priority score:");
        for (Meeting meeting : remainingMeetings) {
            System.out.println("- " + meeting.getTitle() + " (Score: " + calculateMeetingScore(meeting) + ")");
        }
        
        List<OccupiedSlot> occupiedSlots = new ArrayList<>();
        
        for (Meeting meeting : remainingMeetings) {
            System.out.println("\nTrying to schedule: " + meeting.getTitle());
            ScheduledMeeting bestSlot = findBestSlot(meeting, availabilities, occupiedSlots);
            
            if (bestSlot != null) {
                System.out.println("✓ Scheduled: " + meeting.getTitle() + " at " + bestSlot.getScheduledStart());
                scheduledMeetings.add(bestSlot);
                occupiedSlots.add(new OccupiedSlot(
                    bestSlot.getScheduledStart(), 
                    bestSlot.getScheduledEnd()
                ));
            } else {
                System.out.println("✗ Could not schedule: " + meeting.getTitle());
            }
        }
        
        System.out.println("=== OPTIMIZATION END ===");
        System.out.println("Successfully scheduled: " + scheduledMeetings.size() + " meetings");
        
        return new OptimizedScheduleResult(scheduledMeetings, calculateOverallScore(scheduledMeetings));
    }
    
    private ScheduledMeeting findBestSlot(Meeting meeting, List<Availability> availabilities, List<OccupiedSlot> occupiedSlots) {
        System.out.println("  Finding slot for: " + meeting.getTitle() + " (Duration: " + meeting.getDurationMinutes() + "min, Deadline: " + meeting.getDeadline() + ")");
        
        ScheduledMeeting bestSlot = null;
        double bestScore = Double.MIN_VALUE;
        
        for (Availability availability : availabilities) {
            System.out.println("  Checking availability: " + availability.getStartTime() + " to " + availability.getEndTime());
            LocalDateTime currentTime = availability.getStartTime();
            
            // Try every 15-minute slot within availability
            while (currentTime.plusMinutes(meeting.getDurationMinutes()).isBefore(availability.getEndTime()) ||
                   currentTime.plusMinutes(meeting.getDurationMinutes()).equals(availability.getEndTime())) {
                
                // Safety check to prevent infinite loop
                if (currentTime.isAfter(availability.getEndTime())) {
                    break;
                }
                
                LocalDateTime proposedEnd = currentTime.plusMinutes(meeting.getDurationMinutes());
                
                System.out.println("    Trying slot: " + currentTime + " to " + proposedEnd);
                
                // Check if meeting ends before deadline
                if (proposedEnd.isAfter(meeting.getDeadline())) {
                    System.out.println("    ✗ Meeting would end after deadline");
                    break;
                }
                
                // Check for conflicts with already scheduled meetings
                if (!hasConflict(currentTime, proposedEnd, occupiedSlots)) {
                    double score = calculateSchedulingScore(meeting, currentTime);
                    System.out.println("    ✓ No conflict, score: " + score);
                    
                    if (score > bestScore) {
                        bestScore = score;
                        bestSlot = new ScheduledMeeting(
                            meeting,
                            currentTime,
                            proposedEnd,
                            score
                        );
                        System.out.println("    ★ New best slot found!");
                    }
                } else {
                    System.out.println("    ✗ Conflict with existing meeting");
                }
                
                // Move to next 15-minute slot
                currentTime = currentTime.plusMinutes(15);
            }
        }
        
        if (bestSlot != null) {
            System.out.println("  Best slot found: " + bestSlot.getScheduledStart() + " to " + bestSlot.getScheduledEnd() + " (score: " + bestSlot.getScore() + ")");
        } else {
            System.out.println("  No suitable slot found");
        }
        
        return bestSlot;
    }
    
    private boolean hasConflict(LocalDateTime start, LocalDateTime end, List<OccupiedSlot> occupiedSlots) {
        return occupiedSlots.stream().anyMatch(occupied -> 
            !(end.isBefore(occupied.getStart()) || start.isAfter(occupied.getEnd()))
        );
    }
    
    private double calculateMeetingScore(Meeting meeting) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = meeting.getDeadline();
        
        // Priority score (normalized to 0-1)
        double priorityScore = meeting.getPriority() / 10.0;
        
        // Deadline urgency score (closer deadline = higher score)
        double deadlineScore = 0.0;
        if (deadline != null) {
            long hoursUntilDeadline = ChronoUnit.HOURS.between(now, deadline);
            deadlineScore = Math.max(0, 1.0 - (hoursUntilDeadline / 168.0)); // 1 week max
        }
        
        // Duration score (shorter meetings preferred)
        double durationScore = 1.0 - Math.min(meeting.getDurationMinutes() / 240.0, 1.0); // 4 hours max
        
        return (priorityScore * PRIORITY_WEIGHT) + 
               (deadlineScore * DEADLINE_WEIGHT) + 
               (durationScore * DURATION_WEIGHT);
    }
    
    private double calculateSchedulingScore(Meeting meeting, LocalDateTime scheduledTime) {
        LocalDateTime deadline = meeting.getDeadline();
        
        // Priority score
        double priorityScore = meeting.getPriority() / 10.0;
        
        // Deadline urgency score based on scheduled time
        double deadlineScore = 0.0;
        if (deadline != null) {
            long hoursUntilDeadline = ChronoUnit.HOURS.between(scheduledTime, deadline);
            deadlineScore = Math.max(0, 1.0 - (hoursUntilDeadline / 168.0));
        }
        
        // Duration score
        double durationScore = 1.0 - Math.min(meeting.getDurationMinutes() / 240.0, 1.0);
        
        return (priorityScore * PRIORITY_WEIGHT) + 
               (deadlineScore * DEADLINE_WEIGHT) + 
               (durationScore * DURATION_WEIGHT);
    }
    
    private double calculateOverallScore(List<ScheduledMeeting> scheduledMeetings) {
        return scheduledMeetings.stream()
                .mapToDouble(ScheduledMeeting::getScore)
                .average()
                .orElse(0.0);
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduledMeeting {
        private Meeting meeting;
        private LocalDateTime scheduledStart;
        private LocalDateTime scheduledEnd;
        private double score;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OccupiedSlot {
        private LocalDateTime start;
        private LocalDateTime end;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizedScheduleResult {
        private List<ScheduledMeeting> scheduledMeetings;
        private double optimizationScore; // Changed from overallScore to match frontend
    }
}

