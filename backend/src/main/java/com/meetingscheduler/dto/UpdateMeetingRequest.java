package com.meetingscheduler.dto;

import com.meetingscheduler.entity.Meeting;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateMeetingRequest {
    
    private String title;
    private String description;
    
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;
    
    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 10, message = "Priority must be at most 10")
    private Integer priority;
    
    private LocalDateTime deadline;
    private Meeting.MeetingStatus status;
}






