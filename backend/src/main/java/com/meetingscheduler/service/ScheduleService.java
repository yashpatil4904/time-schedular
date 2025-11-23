package com.meetingscheduler.service;

import com.meetingscheduler.algorithm.ScheduleOptimizer;
import com.meetingscheduler.entity.*;
import com.meetingscheduler.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {
    
    private final ScheduleRepository scheduleRepository;
    private final MeetingRepository meetingRepository;
    private final AvailabilityRepository availabilityRepository;
    private final NotificationRepository notificationRepository;
    private final ScheduleOptimizer scheduleOptimizer;
    
    public List<Schedule> getSchedulesByUser(UUID userId) {
        log.info("Fetching schedules for user ID: {}", userId);
        // Return schedules sorted by optimization score (highest first) so highest priority meetings appear first
        List<Schedule> schedules = scheduleRepository.findByUserIdOrderByOptimizationScoreDesc(userId);
        log.info("Found {} schedules for user {}, sorted by optimization score (highest first)", schedules.size(), userId);
        return schedules;
    }
    
    @Transactional
    public ScheduleOptimizer.OptimizedScheduleResult optimizeSchedule(UUID userId) {
        log.info("\n" + "=".repeat(80));
        log.info("🎯 SCHEDULE SERVICE: Starting optimization for user: {}", userId);
        log.info("=".repeat(80));
        
        log.info("Optimizing schedule for user ID: {}", userId);
        
        // Get pending meetings for the user
        List<Meeting> pendingMeetings = meetingRepository.findByUserIdAndStatusOrderByPriorityDescDeadlineAsc(
            userId, Meeting.MeetingStatus.pending);
        
        System.out.println("📋 Retrieved " + pendingMeetings.size() + " pending meetings from database");
        log.info("Found {} pending meetings for user {}", pendingMeetings.size(), userId);
        
        if (pendingMeetings.isEmpty()) {
            System.out.println("❌ ERROR: No pending meetings found!");
            log.warn("No pending meetings found for user {}", userId);
            throw new IllegalArgumentException("No pending meetings found for optimization");
        }
        
        // Get user's availability
        List<Availability> availabilities = availabilityRepository.findByUserIdOrderByStartTimeAsc(userId);
        
        System.out.println("⏰ Retrieved " + availabilities.size() + " availability slots from database");
        log.info("Found {} availability slots for user {}", availabilities.size(), userId);
        
        if (availabilities.isEmpty()) {
            System.out.println("❌ ERROR: No availability slots found!");
            log.warn("No availability slots found for user {}", userId);
            throw new IllegalArgumentException("No availability slots found. Please set your availability first.");
        }
        
        // Log meeting details for debugging
        System.out.println("\n📝 MEETING DETAILS FROM DATABASE:");
        for (int i = 0; i < pendingMeetings.size(); i++) {
            Meeting meeting = pendingMeetings.get(i);
            System.out.println(String.format("   %d. %s (ID: %s)", i + 1, meeting.getTitle(), meeting.getId()));
            System.out.println(String.format("      Priority: %d | Duration: %d min | Deadline: %s | Status: %s",
                meeting.getPriority(), meeting.getDurationMinutes(), meeting.getDeadline(), meeting.getStatus()));
            log.info("Meeting: {} - Priority: {}, Duration: {}min, Deadline: {}", 
                meeting.getTitle(), meeting.getPriority(), meeting.getDurationMinutes(), meeting.getDeadline());
        }
        
        // Log availability details for debugging
        System.out.println("\n📅 AVAILABILITY DETAILS FROM DATABASE:");
        for (int i = 0; i < availabilities.size(); i++) {
            Availability availability = availabilities.get(i);
            long hours = java.time.temporal.ChronoUnit.HOURS.between(availability.getStartTime(), availability.getEndTime());
            System.out.println(String.format("   %d. %s → %s (%d hours)", i + 1,
                availability.getStartTime(), availability.getEndTime(), hours));
            log.info("Availability: {} to {}", availability.getStartTime(), availability.getEndTime());
        }
        
        System.out.println("\n🚀 Calling ScheduleOptimizer.optimizeSchedule()...\n");
        
        // Run optimization algorithm
        ScheduleOptimizer.OptimizedScheduleResult result = scheduleOptimizer.optimizeSchedule(
            pendingMeetings, availabilities);
        
        System.out.println("\n💾 SAVING SCHEDULED MEETINGS TO DATABASE:");
        
        // First, delete any existing schedules for these meetings to avoid duplicates
        for (ScheduleOptimizer.ScheduledMeeting scheduledMeeting : result.getScheduledMeetings()) {
            Meeting meeting = scheduledMeeting.getMeeting();
            List<Schedule> existingSchedules = scheduleRepository.findByMeetingId(meeting.getId());
            if (!existingSchedules.isEmpty()) {
                System.out.println(String.format("   🗑️  Deleting %d existing schedule(s) for meeting '%s'", 
                    existingSchedules.size(), meeting.getTitle()));
                scheduleRepository.deleteAll(existingSchedules);
            }
        }
        
        // Save scheduled meetings
        for (int i = 0; i < result.getScheduledMeetings().size(); i++) {
            ScheduleOptimizer.ScheduledMeeting scheduledMeeting = result.getScheduledMeetings().get(i);
            
            // Update meeting status to scheduled
            Meeting meeting = scheduledMeeting.getMeeting();
            meeting.setStatus(Meeting.MeetingStatus.scheduled);
            meetingRepository.save(meeting);
            System.out.println(String.format("   %d. Updated meeting '%s' status to SCHEDULED", i + 1, meeting.getTitle()));
            
            // Get individual meeting score (not the overall average)
            double individualScore = scheduledMeeting.getScore();
            log.info("      ════════════════════════════════════════════════════════");
            log.info("      📊 MEETING: {}", meeting.getTitle());
            log.info("      🎯 INDIVIDUAL SCORE: {}", String.format("%.3f", individualScore));
            log.info("      📋 Details: Priority={}/10 | Duration={} min | Deadline={}",
                meeting.getPriority(), meeting.getDurationMinutes(), meeting.getDeadline());
            log.info("      ════════════════════════════════════════════════════════");
            
            // Create schedule record
            Schedule schedule = new Schedule();
            schedule.setMeeting(meeting);
            schedule.setUser(meeting.getUser());
            schedule.setScheduledStart(scheduledMeeting.getScheduledStart());
            schedule.setScheduledEnd(scheduledMeeting.getScheduledEnd());
            // Use individual meeting score, not the overall optimization score
            schedule.setOptimizationScore(individualScore);
            
            Schedule savedSchedule = scheduleRepository.save(schedule);
            log.info("      ✅ Created schedule record (ID: {})", savedSchedule.getId());
            log.info("      💾 SAVED TO DATABASE - Score: {}", String.format("%.3f", savedSchedule.getOptimizationScore()));
            
            // Verify score was saved correctly
            if (Math.abs(savedSchedule.getOptimizationScore() - individualScore) < 0.001) {
                log.info("      ✅ VERIFIED: Score saved correctly ({} = {})", 
                    String.format("%.3f", savedSchedule.getOptimizationScore()), String.format("%.3f", individualScore));
            } else {
                log.warn("      ⚠️  WARNING: Score mismatch! Saved: {}, Expected: {}", 
                    String.format("%.3f", savedSchedule.getOptimizationScore()), String.format("%.3f", individualScore));
            }
            log.info("");
        }
        
        // Send notification
        if (!pendingMeetings.isEmpty()) {
            createNotification(meetingRepository.findById(pendingMeetings.get(0).getId()).get().getUser(),
                              Notification.NotificationType.SCHEDULE_OPTIMIZED,
                              String.format("Schedule optimized! %d meetings scheduled.", result.getScheduledMeetings().size()));
            System.out.println("📧 Notification sent to user");
        }
        
        log.info("\n" + "=".repeat(80));
        log.info("✅ SCHEDULE SERVICE: Optimization completed successfully!");
        log.info("=".repeat(80));
        log.info("📊 SUMMARY:");
        log.info("   • Total meetings scheduled: {}", result.getScheduledMeetings().size());
        log.info("   • Overall optimization score (average): {}", String.format("%.3f", result.getOptimizationScore()));
        log.info("");
        log.info("🎯 INDIVIDUAL MEETING SCORES (SAVED TO DATABASE):");
        log.info("=".repeat(80));
        for (int i = 0; i < result.getScheduledMeetings().size(); i++) {
            ScheduleOptimizer.ScheduledMeeting sm = result.getScheduledMeetings().get(i);
            log.info("   #{}: {}", i + 1, sm.getMeeting().getTitle());
            log.info("        SCORE: {} | Priority: {}/10 | Duration: {} min | Deadline: {}", 
                String.format("%.3f", sm.getScore()), sm.getMeeting().getPriority(), sm.getMeeting().getDurationMinutes(), sm.getMeeting().getDeadline());
            log.info("        Time: {} → {}", sm.getScheduledStart(), sm.getScheduledEnd());
            log.info("");
        }
        log.info("=".repeat(80));
        log.info("✅ All scores have been saved to the database!");
        log.info("=".repeat(80) + "\n");
        
        log.info("Schedule optimization completed. {} meetings scheduled.", result.getScheduledMeetings().size());
        return result;
    }
    
    @Transactional
    public Schedule createCustomSchedule(UUID userId, UUID meetingId, String startTime, String endTime) {
        log.info("Creating custom schedule for user {} and meeting {}", userId, meetingId);
        
        // Get the meeting
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("Meeting not found"));
        
        // Verify the meeting belongs to the user
        if (!meeting.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Meeting does not belong to the specified user");
        }
        
        // Update meeting status to scheduled
        meeting.setStatus(Meeting.MeetingStatus.scheduled);
        meetingRepository.save(meeting);
        
        // Create custom schedule
        Schedule schedule = new Schedule();
        schedule.setMeeting(meeting);
        schedule.setUser(meeting.getUser());
        schedule.setScheduledStart(LocalDateTime.parse(startTime));
        schedule.setScheduledEnd(LocalDateTime.parse(endTime));
        schedule.setOptimizationScore(1.0); // Custom schedules get perfect score
        
        Schedule savedSchedule = scheduleRepository.save(schedule);
        
        // Notify user about custom schedule
        createNotification(meeting.getUser(), Notification.NotificationType.MEETING_SCHEDULED,
            "Meeting '" + meeting.getTitle() + "' has been custom scheduled by your secretary");
        
        log.info("Custom schedule created successfully for meeting: {}", meeting.getTitle());
        return savedSchedule;
    }
    
    @Transactional
    public Schedule updateMeetingSchedule(UUID userId, UUID meetingId, String startTime, String endTime) {
        log.info("Updating meeting schedule for user {} and meeting {}", userId, meetingId);
        
        // Get the meeting first
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("Meeting not found"));
        
        // Verify the meeting belongs to the user
        if (!meeting.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Meeting does not belong to the specified user");
        }
        
        // Try to find existing schedule for this meeting
        Optional<Schedule> existingScheduleOpt = scheduleRepository.findByMeetingIdAndUserId(meetingId, userId);
        
        Schedule schedule;
        if (existingScheduleOpt.isPresent()) {
            // Update existing schedule
            schedule = existingScheduleOpt.get();
            schedule.setScheduledStart(LocalDateTime.parse(startTime));
            schedule.setScheduledEnd(LocalDateTime.parse(endTime));
            log.info("Updating existing schedule for meeting: {}", meeting.getTitle());
        } else {
            // Create new schedule if none exists
            schedule = new Schedule();
            schedule.setMeeting(meeting);
            schedule.setUser(meeting.getUser());
            schedule.setScheduledStart(LocalDateTime.parse(startTime));
            schedule.setScheduledEnd(LocalDateTime.parse(endTime));
            schedule.setOptimizationScore(1.0);
            
            // Update meeting status to scheduled
            meeting.setStatus(Meeting.MeetingStatus.scheduled);
            meetingRepository.save(meeting);
            log.info("Creating new schedule for meeting: {}", meeting.getTitle());
        }
        
        Schedule savedSchedule = scheduleRepository.save(schedule);
        
        // Notify user about schedule update
        createNotification(meeting.getUser(), Notification.NotificationType.MEETING_SCHEDULED,
            "Meeting '" + meeting.getTitle() + "' schedule has been updated by your secretary");
        
        log.info("Meeting schedule updated successfully for meeting: {}", meeting.getTitle());
        return savedSchedule;
    }
    
    private void createNotification(User user, Notification.NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setMessage(message);
        notification.setUser(user);
        notification.setIsRead(false);
        notificationRepository.save(notification);
    }
}