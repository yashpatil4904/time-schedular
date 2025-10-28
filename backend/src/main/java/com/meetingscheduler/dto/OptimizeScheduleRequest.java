package com.meetingscheduler.dto;

import com.meetingscheduler.entity.Meeting;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OptimizeScheduleRequest {
    
    private List<Meeting> meetings;
    private List<AvailabilitySlot> availabilitySlots;
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AvailabilitySlot {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}



