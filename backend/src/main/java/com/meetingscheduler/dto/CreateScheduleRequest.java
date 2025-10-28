package com.meetingscheduler.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateScheduleRequest {
    
    private TimeFrame timeFrame;
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TimeFrame {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }
}



