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
        return scheduleRepository.findByUserIdOrderByScheduledStartAsc(userId);
    }
    
    @Transactional
    public ScheduleOptimizer.OptimizedScheduleResult optimizeSchedule(UUID userId) {
        log.info("Optimizing schedule for user ID: {}", userId);
        
        // Get pending meetings for the user
        List<Meeting> pendingMeetings = meetingRepository.findByUserIdAndStatusOrderByPriorityDescDeadlineAsc(
            userId, Meeting.MeetingStatus.pending);
        
        log.info("Found {} pending meetings for user {}", pendingMeetings.size(), userId);
        
        if (pendingMeetings.isEmpty()) {
            log.warn("No pending meetings found for user {}", userId);
            throw new IllegalArgumentException("No pending meetings found for optimization");
        }
        
        // Get user's availability
        List<Availability> availabilities = availabilityRepository.findByUserIdOrderByStartTimeAsc(userId);
        
        log.info("Found {} availability slots for user {}", availabilities.size(), userId);
        
        if (availabilities.isEmpty()) {
            log.warn("No availability slots found for user {}", userId);
            throw new IllegalArgumentException("No availability slots found. Please set your availability first.");
        }
        
        // Log meeting details for debugging
        for (Meeting meeting : pendingMeetings) {
            log.info("Meeting: {} - Priority: {}, Duration: {}min, Deadline: {}", 
                meeting.getTitle(), meeting.getPriority(), meeting.getDurationMinutes(), meeting.getDeadline());
        }
        
        // Log availability details for debugging
        for (Availability availability : availabilities) {
            log.info("Availability: {} to {}", availability.getStartTime(), availability.getEndTime());
        }
        
        // Run optimization algorithm
        ScheduleOptimizer.OptimizedScheduleResult result = scheduleOptimizer.optimizeSchedule(
            pendingMeetings, availabilities);
        
        // Save scheduled meetings
        for (ScheduleOptimizer.ScheduledMeeting scheduledMeeting : result.getScheduledMeetings()) {
            // Update meeting status to scheduled
            Meeting meeting = scheduledMeeting.getMeeting();
            meeting.setStatus(Meeting.MeetingStatus.scheduled);
            meetingRepository.save(meeting);
            
            // Create schedule record
            Schedule schedule = new Schedule();
            schedule.setMeeting(meeting);
            schedule.setUser(meeting.getUser());
            schedule.setScheduledStart(scheduledMeeting.getScheduledStart());
            schedule.setScheduledEnd(scheduledMeeting.getScheduledEnd());
            schedule.setOptimizationScore(result.getOptimizationScore());
            
            scheduleRepository.save(schedule);
        }
        
        // Send notification
        createNotification(meetingRepository.findById(pendingMeetings.get(0).getId()).get().getUser(),
                          Notification.NotificationType.SCHEDULE_OPTIMIZED,
                          String.format("Schedule optimized! %d meetings scheduled.", result.getScheduledMeetings().size()));
        
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