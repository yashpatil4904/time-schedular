package com.meetingscheduler.repository;

import com.meetingscheduler.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
    
    List<Meeting> findByUserId(UUID userId);
    
    List<Meeting> findByStatus(Meeting.MeetingStatus status);
    
    List<Meeting> findByUserIdAndStatus(UUID userId, Meeting.MeetingStatus status);
    
    List<Meeting> findByUserIdAndStatusOrderByPriorityDescDeadlineAsc(UUID userId, Meeting.MeetingStatus status);
}