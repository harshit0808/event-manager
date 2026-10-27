package com.example.event_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationDTO {
    private Long registrationId;
    private String qrCodeData;
    private LocalDateTime registrationDate;

    private String eventTitle;
    private String eventLocation;
    private LocalDateTime eventDate;
}