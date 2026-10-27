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
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime eventDate;
    private int availableSpots;
    private OrganizerDTO organizer; 
}