package com.example.event_manager.controller;

import com.example.event_manager.domain.User;
import com.example.event_manager.dto.EventDTO;
import com.example.event_manager.dto.AttendeeDTO;
import com.example.event_manager.service.EventService;
import com.example.event_manager.dto.CheckInRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/organizer")
public class OrganizerController {

    private final EventService eventService;
    public OrganizerController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/my-events")
    public ResponseEntity<List<EventDTO>> getMyEvents(Authentication authentication) {

        User loggedInOrganizer = (User) authentication.getPrincipal();

        List<EventDTO> events = eventService.getEventsForOrganizer(loggedInOrganizer);

        return ResponseEntity.ok(events);
    }

    @GetMapping("/my-events/{id}/attendees")
    public ResponseEntity<List<AttendeeDTO>> getEventAttendees(
            @PathVariable Long id,
            Authentication authentication) {

        User loggedInOrganizer = (User) authentication.getPrincipal();

        List<AttendeeDTO> attendees = eventService.getEventAttendees(id, loggedInOrganizer);

        return ResponseEntity.ok(attendees);
    }

    @PostMapping("/my-events/{id}/check-in")
    public ResponseEntity<AttendeeDTO> checkIn(
            @PathVariable Long id,
            @Valid @RequestBody CheckInRequest request,
            Authentication authentication) {

        User loggedInOrganizer = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                eventService.checkInAttendee(id, request.getQrCodeData(), loggedInOrganizer));
    }
}
