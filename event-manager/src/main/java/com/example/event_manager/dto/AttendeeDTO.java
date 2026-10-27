package com.example.event_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendeeDTO {
    private Long registrationId;
    private String username;
    private String email;
    private LocalDateTime registrationDate;
    private boolean checkedIn;
    private LocalDateTime checkedInAt;
}
