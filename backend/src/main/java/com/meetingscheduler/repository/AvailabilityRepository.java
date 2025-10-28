package com.meetingscheduler.repository;

import com.meetingscheduler.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {
    
    List<Availability> findByUserId(UUID userId);
    
    List<Availability> findByUserIdOrderByStartTimeAsc(UUID userId);
}
