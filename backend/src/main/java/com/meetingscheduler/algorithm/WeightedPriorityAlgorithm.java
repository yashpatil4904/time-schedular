package com.meetingscheduler.algorithm;

import com.meetingscheduler.entity.Meeting;
import com.meetingscheduler.entity.Schedule;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class WeightedPriorityAlgorithm {
    
    private static final double PRIORITY_WEIGHT = 0.4;
    private static final double DEADLINE_WEIGHT = 0.4;
    private static final double DURATION_WEIGHT = 0.2;
    
    public OptimizedScheduleResult optimizeSchedule(List<Meeting> meetings, 
                                                   List<AvailabilitySlot> availabilitySlots) {
        
        List<ScheduledMeeting> scheduledMeetings = new ArrayList<>();
        List<Meeting> remainingMeetings = new ArrayList<>(meetings);
        
        // Sort meetings by priority score (highest first)
        remainingMeetings.sort((a, b) -> Double.compare(calculateWeight(b), calculateWeight(a)));
        
        List<OccupiedSlot> occupiedSlots = new ArrayList<>();
        
        for (Meeting meeting : remainingMeetings) {
            ScheduledMeeting bestSlot = findBestSlot(meeting, availabilitySlots, occupiedSlots);
            
            if (bestSlot != null) {
                scheduledMeetings.add(bestSlot);
                occupiedSlots.add(new OccupiedSlot(
                    bestSlot.getScheduledStart(), 
                    bestSlot.getScheduledEnd()
                ));
            }
        }
        
        return new OptimizedScheduleResult(scheduledMeetings, calculateOptimizationScore(scheduledMeetings));
    }
    
    private ScheduledMeeting findBestSlot(Meeting meeting, 
                                        List<AvailabilitySlot> availabilitySlots, 
                                        List<OccupiedSlot> occupiedSlots) {
        
        ScheduledMeeting bestSlot = null;
        double bestScore = Double.MIN_VALUE;
        
        for (AvailabilitySlot slot : availabilitySlots) {
            LocalDateTime currentTime = slot.getStartTime();
            
            while (currentTime.plusMinutes(meeting.getDuration()).isBefore(slot.getEndTime()) ||
                   currentTime.plusMinutes(meeting.getDuration()).equals(slot.getEndTime())) {
                
                LocalDateTime proposedEnd = currentTime.plusMinutes(meeting.getDuration());
                
                // Check if meeting ends before deadline
                if (meeting.getDeadline() != null && proposedEnd.isAfter(meeting.getDeadline())) {
                    break;
                }
                
                // Check for conflicts
                if (!hasConflict(currentTime, proposedEnd, occupiedSlots)) {
                    double score = calculateSchedulingScore(meeting, currentTime);
                    
                    if (score > bestScore) {
                        bestScore = score;
                        bestSlot = new ScheduledMeeting(
                            meeting,
                            currentTime,
                            proposedEnd,
                            score
                        );
                    }
                }
                
                // Move to next 15-minute slot
                currentTime = currentTime.plusMinutes(15);
            }
        }
        
        return bestSlot;
    }
    
    private boolean hasConflict(LocalDateTime start, LocalDateTime end, List<OccupiedSlot> occupiedSlots) {
        return occupiedSlots.stream().anyMatch(occupied -> 
            !(end.isBefore(occupied.getStart()) || start.isAfter(occupied.getEnd()))
        );
    }
    
    public double calculateWeight(Meeting meeting) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = meeting.getDeadline();
        
        double priorityScore = meeting.getPriority() / 10.0;
        double deadlineScore = 0.0;
        double durationScore = 1.0 - Math.min(meeting.getDuration() / 240.0, 1.0);
        
        if (deadline != null) {
            long hoursUntilDeadline = ChronoUnit.HOURS.between(now, deadline);
            deadlineScore = Math.max(0, 1.0 - (hoursUntilDeadline / 720.0)); // 30 days max
        }
        
        return (priorityScore * PRIORITY_WEIGHT) + 
               (deadlineScore * DEADLINE_WEIGHT) + 
               (durationScore * DURATION_WEIGHT);
    }
    
    private double calculateSchedulingScore(Meeting meeting, LocalDateTime scheduledTime) {
        LocalDateTime deadline = meeting.getDeadline();
        
        double priorityScore = meeting.getPriority() / 10.0;
        double deadlineScore = 0.0;
        double durationScore = 1.0 - Math.min(meeting.getDuration() / 240.0, 1.0);
        
        if (deadline != null) {
            long hoursUntilDeadline = ChronoUnit.HOURS.between(scheduledTime, deadline);
            deadlineScore = Math.max(0, 1.0 - (hoursUntilDeadline / 720.0));
        }
        
        return (priorityScore * PRIORITY_WEIGHT) + 
               (deadlineScore * DEADLINE_WEIGHT) + 
               (durationScore * DURATION_WEIGHT);
    }
    
    private double calculateOptimizationScore(List<ScheduledMeeting> scheduledMeetings) {
        return scheduledMeetings.stream()
                .mapToDouble(ScheduledMeeting::getScore)
                .average()
                .orElse(0.0);
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailabilitySlot {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
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
    public static class ScheduledMeeting {
        private Meeting meeting;
        private LocalDateTime scheduledStart;
        private LocalDateTime scheduledEnd;
        private double score;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizedScheduleResult {
        private List<ScheduledMeeting> scheduledMeetings;
        private double optimizationScore;
    }
}



