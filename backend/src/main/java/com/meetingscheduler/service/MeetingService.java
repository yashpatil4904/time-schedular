package com.meetingscheduler.service;

import com.meetingscheduler.entity.Meeting;
import com.meetingscheduler.entity.User;
import com.meetingscheduler.entity.Participation;
import com.meetingscheduler.repository.MeetingRepository;
import com.meetingscheduler.repository.NotificationRepository;
import com.meetingscheduler.repository.ParticipationRepository;
import com.meetingscheduler.repository.UserRepository;
import com.meetingscheduler.entity.Notification;
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
public class MeetingService {
    
    private final MeetingRepository meetingRepository;
    private final NotificationRepository notificationRepository;
    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    
    public List<Meeting> getMeetingsByUser(UUID userId) {
        log.info("Fetching meetings for user ID: {}", userId);
        return meetingRepository.findByUserId(userId);
    }
    
    public List<Meeting> getPendingMeetings(UUID userId) {
        log.info("Fetching pending meetings for user ID: {}", userId);
        return meetingRepository.findByUserIdAndStatusOrderByPriorityDescDeadlineAsc(userId, Meeting.MeetingStatus.pending);
    }
    
    @Transactional
    public Meeting createMeeting(Meeting meeting, User user) {
        log.info("Creating new meeting: {}", meeting.getTitle());
        
        // Validate meeting
        validateMeeting(meeting);
        
        meeting.setUser(user);
        meeting.setStatus(Meeting.MeetingStatus.pending);
        
        Meeting savedMeeting = meetingRepository.save(meeting);
        
        // Send notification
        createNotification(user, Notification.NotificationType.MEETING_CREATED, 
                          "New meeting created: " + savedMeeting.getTitle());
        
        log.info("Meeting created with ID: {}", savedMeeting.getId());
        return savedMeeting;
    }
    
    @Transactional
    public Meeting updateMeeting(UUID meetingId, Meeting meetingDetails, UUID userId) {
        log.info("Updating meeting with ID: {}", meetingId);
        
        Meeting existingMeeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found with ID: " + meetingId));
        
        // Check if user owns this meeting
        if (!existingMeeting.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only update your own meetings");
        }
        
        // Update fields
        if (meetingDetails.getTitle() != null) {
            existingMeeting.setTitle(meetingDetails.getTitle());
        }
        if (meetingDetails.getDescription() != null) {
            existingMeeting.setDescription(meetingDetails.getDescription());
        }
        if (meetingDetails.getPriority() != null) {
            existingMeeting.setPriority(meetingDetails.getPriority());
        }
        if (meetingDetails.getDurationMinutes() != null) {
            existingMeeting.setDurationMinutes(meetingDetails.getDurationMinutes());
        }
        if (meetingDetails.getDeadline() != null) {
            existingMeeting.setDeadline(meetingDetails.getDeadline());
        }
        
        return meetingRepository.save(existingMeeting);
    }
    
    @Transactional
    public void deleteMeeting(UUID meetingId, UUID userId) {
        log.info("Deleting meeting with ID: {}", meetingId);
        
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found with ID: " + meetingId));
        
        // Check if user owns this meeting
        if (!meeting.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own meetings");
        }
        
        meetingRepository.delete(meeting);
        
        // Send notification
        createNotification(meeting.getUser(), Notification.NotificationType.MEETING_CANCELLED, 
                          "Meeting cancelled: " + meeting.getTitle());
    }
    
    @Transactional
    public Meeting markMeetingAsCompleted(UUID meetingId, UUID userId) {
        log.info("Marking meeting as completed with ID: {}", meetingId);
        
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found with ID: " + meetingId));
        
        // Check if user owns this meeting
        if (!meeting.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only mark your own meetings as completed");
        }
        
        meeting.setStatus(Meeting.MeetingStatus.completed);
        Meeting updatedMeeting = meetingRepository.save(meeting);
        
        // Send notification
        createNotification(meeting.getUser(), Notification.NotificationType.MEETING_CREATED, 
                          "Meeting completed: " + meeting.getTitle());
        
        log.info("Meeting marked as completed: {}", meetingId);
        return updatedMeeting;
    }
    
    private void validateMeeting(Meeting meeting) {
        if (meeting.getTitle() == null || meeting.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Meeting title is required");
        }
        
        if (meeting.getPriority() == null || meeting.getPriority() < 1 || meeting.getPriority() > 10) {
            throw new IllegalArgumentException("Meeting priority must be between 1 and 10");
        }
        
        if (meeting.getDurationMinutes() == null || meeting.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Meeting duration must be positive");
        }
        
        if (meeting.getDeadline() == null) {
            throw new IllegalArgumentException("Meeting deadline is required");
        }
        
        if (meeting.getDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Meeting deadline cannot be in the past");
        }
    }
    
    @Transactional
    public Meeting createMeetingWithParticipants(Meeting meeting, User organizer, List<UUID> participantIds) {
        log.info("Creating meeting with participants: {}", meeting.getTitle());
        
        // Validate and create meeting
        validateMeeting(meeting);
        meeting.setUser(organizer);
        meeting.setStatus(Meeting.MeetingStatus.pending);
        
        Meeting savedMeeting = meetingRepository.save(meeting);
        
        // Add organizer as participant
        Participation organizerParticipation = new Participation();
        organizerParticipation.setMeeting(savedMeeting);
        organizerParticipation.setUser(organizer);
        organizerParticipation.setRole(Participation.ParticipationRole.ORGANIZER);
        organizerParticipation.setResponseStatus(Participation.ResponseStatus.ACCEPTED);
        participationRepository.save(organizerParticipation);
        
        // Add other participants
        if (participantIds != null && !participantIds.isEmpty()) {
            for (UUID participantId : participantIds) {
                if (!participantId.equals(organizer.getId())) {
                    userRepository.findById(participantId).ifPresent(participant -> {
                        Participation participation = new Participation();
                        participation.setMeeting(savedMeeting);
                        participation.setUser(participant);
                        participation.setRole(Participation.ParticipationRole.ATTENDEE);
                        participation.setResponseStatus(Participation.ResponseStatus.NO_RESPONSE);
                        participationRepository.save(participation);
                        
                        // Notify participant
                        createNotification(participant, Notification.NotificationType.MEETING_CREATED,
                                "You've been invited to: " + savedMeeting.getTitle());
                    });
                }
            }
        }
        
        // Notify organizer
        createNotification(organizer, Notification.NotificationType.MEETING_CREATED,
                "Meeting created: " + savedMeeting.getTitle());
        
        log.info("Meeting created with {} participants", participantIds != null ? participantIds.size() : 0);
        return savedMeeting;
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