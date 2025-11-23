package com.meetingscheduler.algorithm;

import com.meetingscheduler.entity.Meeting;
import com.meetingscheduler.entity.Availability;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@Slf4j
public class ScheduleOptimizer {
    
    // Weights for optimization algorithm - Priority and Deadline are most important
    private static final double PRIORITY_WEIGHT = 0.6;    // 60% - Most important
    private static final double DEADLINE_WEIGHT = 0.3;    // 30% - Urgent deadlines first
    private static final double DURATION_WEIGHT = 0.1;    // 10% - Shorter meetings preferred
    
    public OptimizedScheduleResult optimizeSchedule(List<Meeting> meetings, List<Availability> availabilities) {
        log.info("\n" + "=".repeat(80));
        log.info("🚀 SCHEDULE OPTIMIZATION STARTED");
        log.info("=".repeat(80));
        log.info("📊 INPUT PARAMETERS:");
        log.info("   • Meetings to schedule: {}", meetings.size());
        log.info("   • Availability slots: {}", availabilities.size());
        log.info("");
        
        // Print all meetings with details
        log.info("📋 MEETINGS TO SCHEDULE:");
        for (int i = 0; i < meetings.size(); i++) {
            Meeting m = meetings.get(i);
            double score = calculateMeetingScore(m);
            log.info("   {}. {}", i + 1, m.getTitle());
            log.info("      Priority: {}/10 | Duration: {} min | Deadline: {} | Score: {:.3f}",
                m.getPriority(), m.getDurationMinutes(), m.getDeadline(), String.format("%.3f", score));
        }
        log.info("");
        
        // Print all availability slots
        System.out.println("⏰ AVAILABILITY SLOTS:");
        for (int i = 0; i < availabilities.size(); i++) {
            Availability a = availabilities.get(i);
            long hours = ChronoUnit.HOURS.between(a.getStartTime(), a.getEndTime());
            System.out.println(String.format("   %d. %s → %s (%d hours)",
                i + 1, a.getStartTime(), a.getEndTime(), hours));
        }
        System.out.println();
        
        List<ScheduledMeeting> scheduledMeetings = new ArrayList<>();
        List<Meeting> remainingMeetings = new ArrayList<>(meetings);
        
        // Calculate scores for all meetings first
        System.out.println("📊 CALCULATING SCORES FOR ALL MEETINGS:");
        System.out.println("-".repeat(80));
        Map<Meeting, Double> meetingScores = new HashMap<>();
        for (Meeting m : remainingMeetings) {
            double score = calculateMeetingScore(m);
            meetingScores.put(m, score);
        }
        System.out.println();
        
        // Sort meetings by priority score (highest first) - GREEDY APPROACH
        // This ensures the MOST IMPORTANT meeting is scheduled FIRST
        remainingMeetings.sort((a, b) -> {
            double scoreA = meetingScores.get(a);
            double scoreB = meetingScores.get(b);
            int comparison = Double.compare(scoreB, scoreA); // Descending order (highest first)
            if (comparison == 0) {
                // If scores are equal, prioritize by deadline (earlier deadline first)
                if (a.getDeadline() != null && b.getDeadline() != null) {
                    comparison = a.getDeadline().compareTo(b.getDeadline());
                } else if (a.getDeadline() != null) {
                    comparison = -1; // a has deadline, b doesn't - a comes first
                } else if (b.getDeadline() != null) {
                    comparison = 1; // b has deadline, a doesn't - b comes first
                }
            }
            return comparison;
        });
        
        log.info("🔢 MEETINGS SORTED BY PRIORITY (HIGHEST SCORE FIRST - WILL BE SCHEDULED IN THIS ORDER):");
        log.info("-".repeat(80));
        for (int i = 0; i < remainingMeetings.size(); i++) {
            Meeting m = remainingMeetings.get(i);
            double score = meetingScores.get(m);
            log.info("   🥇 #{}: {}", i + 1, m.getTitle());
            log.info("      Priority: {}/10 | Deadline: {} | Duration: {} min | TOTAL SCORE: {}",
                m.getPriority(), m.getDeadline(), m.getDurationMinutes(), String.format("%.3f", score));
        }
        log.info("");
        
        List<OccupiedSlot> occupiedSlots = new ArrayList<>();
        
        System.out.println("📅 SCHEDULING PROCESS:");
        System.out.println("-".repeat(80));
        for (int i = 0; i < remainingMeetings.size(); i++) {
            Meeting meeting = remainingMeetings.get(i);
            System.out.println(String.format("\n[%d/%d] Processing: %s", i + 1, remainingMeetings.size(), meeting.getTitle()));
            System.out.println(String.format("   Priority: %d | Duration: %d min | Deadline: %s",
                meeting.getPriority(), meeting.getDurationMinutes(), meeting.getDeadline()));
            
            ScheduledMeeting bestSlot = findBestSlot(meeting, availabilities, occupiedSlots);
            
            if (bestSlot != null) {
                log.info("   ✅ SCHEDULED: {} → {}", bestSlot.getScheduledStart(), bestSlot.getScheduledEnd());
                log.info("   🎯 MEETING SCORE: {} (Priority: {}, Deadline: {}, Duration: {} min)",
                    String.format("%.3f", bestSlot.getScore()), meeting.getPriority(), meeting.getDeadline(), meeting.getDurationMinutes());
                scheduledMeetings.add(bestSlot);
                occupiedSlots.add(new OccupiedSlot(
                    bestSlot.getScheduledStart(), 
                    bestSlot.getScheduledEnd()
                ));
            } else {
                System.out.println("   ❌ FAILED: Could not find suitable time slot");
            }
        }
        
        double overallScore = calculateOverallScore(scheduledMeetings);
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ OPTIMIZATION COMPLETED");
        System.out.println("=".repeat(80));
        System.out.println("📊 RESULTS SUMMARY:");
        System.out.println(String.format("   • Successfully scheduled: %d/%d meetings", scheduledMeetings.size(), meetings.size()));
        System.out.println(String.format("   • Overall optimization score (average): %.3f", overallScore));
        System.out.println();
        
        // Print individual scores prominently
        log.info("🎯 INDIVIDUAL MEETING SCORES (as calculated by algorithm):");
        log.info("-".repeat(80));
        for (int i = 0; i < scheduledMeetings.size(); i++) {
            ScheduledMeeting sm = scheduledMeetings.get(i);
            log.info("   #{}: {}", i + 1, sm.getMeeting().getTitle());
            log.info("        Score: {} | Priority: {}/10 | Duration: {} min", 
                String.format("%.3f", sm.getScore()), sm.getMeeting().getPriority(), sm.getMeeting().getDurationMinutes());
        }
        log.info("-".repeat(80));
        log.info("");
        
        if (!scheduledMeetings.isEmpty()) {
            System.out.println("📅 FINAL SCHEDULE WITH SCORES:");
            System.out.println("-".repeat(80));
            for (int i = 0; i < scheduledMeetings.size(); i++) {
                ScheduledMeeting sm = scheduledMeetings.get(i);
                System.out.println(String.format("   🥇 #%d: %s", i + 1, sm.getMeeting().getTitle()));
                System.out.println(String.format("      ⏰ Time: %s → %s", sm.getScheduledStart(), sm.getScheduledEnd()));
                System.out.println(String.format("      📊 SCORE: %.3f", sm.getScore()));
                System.out.println(String.format("      📋 Details: Priority=%d/10 | Duration=%d min | Deadline=%s",
                    sm.getMeeting().getPriority(), sm.getMeeting().getDurationMinutes(), sm.getMeeting().getDeadline()));
                System.out.println();
            }
        }
        
        System.out.println("=".repeat(80) + "\n");
        
        return new OptimizedScheduleResult(scheduledMeetings, overallScore);
    }
    
    private ScheduledMeeting findBestSlot(Meeting meeting, List<Availability> availabilities, List<OccupiedSlot> occupiedSlots) {
        System.out.println("   🔍 Searching for best time slot...");
        
        ScheduledMeeting bestSlot = null;
        double bestScore = Double.MIN_VALUE;
        int slotsChecked = 0;
        int conflictsFound = 0;
        
        for (int i = 0; i < availabilities.size(); i++) {
            Availability availability = availabilities.get(i);
            System.out.println(String.format("   📍 Checking availability slot %d/%d: %s → %s",
                i + 1, availabilities.size(), availability.getStartTime(), availability.getEndTime()));
            
            LocalDateTime currentTime = availability.getStartTime();
            
            // Try every 15-minute slot within availability
            while (currentTime.plusMinutes(meeting.getDurationMinutes()).isBefore(availability.getEndTime()) ||
                   currentTime.plusMinutes(meeting.getDurationMinutes()).equals(availability.getEndTime())) {
                
                // Safety check to prevent infinite loop
                if (currentTime.isAfter(availability.getEndTime())) {
                    break;
                }
                
                LocalDateTime proposedEnd = currentTime.plusMinutes(meeting.getDurationMinutes());
                slotsChecked++;
                
                // Check if meeting ends before deadline
                if (proposedEnd.isAfter(meeting.getDeadline())) {
                    System.out.println(String.format("      ⏰ Slot %s → %s: ❌ Ends after deadline (%s)",
                        currentTime, proposedEnd, meeting.getDeadline()));
                    break;
                }
                
                // Check for conflicts with already scheduled meetings
                if (!hasConflict(currentTime, proposedEnd, occupiedSlots)) {
                    double score = calculateSchedulingScore(meeting, currentTime);
                    
                    // Calculate score breakdown for logging
                    double priorityScore = (meeting.getPriority() / 10.0) * PRIORITY_WEIGHT;
                    long hoursUntilDeadline = ChronoUnit.HOURS.between(currentTime, meeting.getDeadline());
                    double deadlineScore = Math.max(0, 1.0 - (hoursUntilDeadline / 168.0)) * DEADLINE_WEIGHT;
                    double durationScore = (1.0 - Math.min(meeting.getDurationMinutes() / 240.0, 1.0)) * DURATION_WEIGHT;
                    
                    System.out.println(String.format("      ✅ Slot %s → %s: Available (Score: %.3f)",
                        currentTime, proposedEnd, score));
                    System.out.println(String.format("         Breakdown: Priority=%.3f, Deadline=%.3f, Duration=%.3f",
                        priorityScore, deadlineScore, durationScore));
                    
                    if (score > bestScore) {
                        bestScore = score;
                        bestSlot = new ScheduledMeeting(
                            meeting,
                            currentTime,
                            proposedEnd,
                            score
                        );
                        System.out.println(String.format("         ⭐ NEW BEST SLOT! (Previous best: %.3f)", bestScore));
                    }
                } else {
                    conflictsFound++;
                    System.out.println(String.format("      ⚠️  Slot %s → %s: Conflict with existing meeting",
                        currentTime, proposedEnd));
                }
                
                // Move to next 15-minute slot
                currentTime = currentTime.plusMinutes(15);
            }
        }
        
        System.out.println(String.format("   📊 Slot search complete: Checked %d slots, Found %d conflicts",
            slotsChecked, conflictsFound));
        
        if (bestSlot != null) {
            System.out.println(String.format("   🎯 BEST SLOT SELECTED: %s → %s (Final Score: %.3f)",
                bestSlot.getScheduledStart(), bestSlot.getScheduledEnd(), bestSlot.getScore()));
        } else {
            System.out.println("   ❌ NO SUITABLE SLOT FOUND");
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
        
        // Priority score (normalized to 0-1) - Higher priority = higher score
        // Priority 10 = 1.0, Priority 1 = 0.1
        double priorityScore = meeting.getPriority() / 10.0;
        
        // Deadline urgency score - URGENT deadlines (closer) = HIGHER score
        // Formula: The closer the deadline, the higher the score
        // If deadline is in 1 hour = 1.0, if deadline is in 1 week = 0.0
        double deadlineScore = 0.0;
        if (deadline != null && deadline.isAfter(now)) {
            long hoursUntilDeadline = ChronoUnit.HOURS.between(now, deadline);
            // Normalize: 0 hours = 1.0, 168 hours (1 week) = 0.0
            // Use exponential decay for urgency - very urgent deadlines get much higher scores
            if (hoursUntilDeadline <= 24) {
                // Within 24 hours - very urgent (0.9 to 1.0)
                deadlineScore = 0.9 + (0.1 * (1.0 - (hoursUntilDeadline / 24.0)));
            } else if (hoursUntilDeadline <= 168) {
                // Within 1 week - urgent (0.5 to 0.9)
                deadlineScore = 0.5 + (0.4 * (1.0 - ((hoursUntilDeadline - 24) / 144.0)));
            } else {
                // More than 1 week - less urgent (0.0 to 0.5)
                deadlineScore = Math.max(0.0, 0.5 * (1.0 - ((hoursUntilDeadline - 168) / 336.0)));
            }
        } else if (deadline != null && deadline.isBefore(now)) {
            // Deadline already passed - very urgent (but should have been scheduled already)
            deadlineScore = 1.0;
        }
        
        // Duration score - SHORTER meetings = HIGHER score
        // 15 min = 1.0, 240 min (4 hours) = 0.0
        double durationScore = 1.0 - Math.min(meeting.getDurationMinutes() / 240.0, 1.0);
        
        // Calculate weighted total score
        double totalScore = (priorityScore * PRIORITY_WEIGHT) + 
                           (deadlineScore * DEADLINE_WEIGHT) + 
                           (durationScore * DURATION_WEIGHT);
        
        long hoursUntilDeadline = deadline != null ? ChronoUnit.HOURS.between(now, deadline) : 0;
        
        log.info("      📊 SCORE CALCULATION for '{}':", meeting.getTitle());
        log.info("         🎯 Priority: {}/10 → Score: {} × Weight: {}% = {}", 
            meeting.getPriority(), String.format("%.3f", priorityScore), (int)(PRIORITY_WEIGHT * 100), String.format("%.3f", priorityScore * PRIORITY_WEIGHT));
        log.info("         ⏰ Deadline: {} ({} hours away) → Score: {} × Weight: {}% = {}", 
            deadline, hoursUntilDeadline, String.format("%.3f", deadlineScore), (int)(DEADLINE_WEIGHT * 100), String.format("%.3f", deadlineScore * DEADLINE_WEIGHT));
        log.info("         ⏱️  Duration: {} min → Score: {} × Weight: {}% = {}", 
            meeting.getDurationMinutes(), String.format("%.3f", durationScore), (int)(DURATION_WEIGHT * 100), String.format("%.3f", durationScore * DURATION_WEIGHT));
        log.info("         ════════════════════════════════════════════════════════");
        log.info("         🏆 TOTAL SCORE: {}", String.format("%.3f", totalScore));
        log.info("");
        
        return totalScore;
    }
    
    private double calculateSchedulingScore(Meeting meeting, LocalDateTime scheduledTime) {
        LocalDateTime deadline = meeting.getDeadline();
        
        // Priority score (normalized to 0-1) - Higher priority = higher score
        double priorityScore = meeting.getPriority() / 10.0;
        
        // Deadline urgency score based on scheduled time
        // Higher score if scheduled time is closer to deadline (but before it)
        double deadlineScore = 0.0;
        if (deadline != null && scheduledTime.isBefore(deadline)) {
            long hoursUntilDeadline = ChronoUnit.HOURS.between(scheduledTime, deadline);
            // Same formula as calculateMeetingScore - urgent deadlines get higher scores
            if (hoursUntilDeadline <= 24) {
                deadlineScore = 0.9 + (0.1 * (1.0 - (hoursUntilDeadline / 24.0)));
            } else if (hoursUntilDeadline <= 168) {
                deadlineScore = 0.5 + (0.4 * (1.0 - ((hoursUntilDeadline - 24) / 144.0)));
            } else {
                deadlineScore = Math.max(0.0, 0.5 * (1.0 - ((hoursUntilDeadline - 168) / 336.0)));
            }
        } else if (deadline != null && scheduledTime.isAfter(deadline)) {
            // Scheduled after deadline - bad, but still give some score
            deadlineScore = 0.1;
        }
        
        // Duration score - shorter meetings = higher score
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

