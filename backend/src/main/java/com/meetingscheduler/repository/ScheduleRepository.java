package com.meetingscheduler.repository;

import com.meetingscheduler.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    
    List<Schedule> findByUserId(UUID userId);
    
    List<Schedule> findByUserIdOrderByScheduledStartAsc(UUID userId);
    
    List<Schedule> findByMeetingId(UUID meetingId);
    
    Optional<Schedule> findByMeetingIdAndUserId(UUID meetingId, UUID userId);
}