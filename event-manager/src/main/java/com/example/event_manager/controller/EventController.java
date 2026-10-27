package com.example.event_manager.controller;

import com.example.event_manager.domain.User;
import com.example.event_manager.dto.CreateEventRequest;
import com.example.event_manager.dto.EventDTO;
import com.example.event_manager.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import com.example.event_manager.dto.MessageResponse;
import com.example.event_manager.service.RegistrationService;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@RestController
@RequestMapping("/api/events") 
public class EventController {

    private final EventService eventService;
    private final RegistrationService registrationService;

    public EventController(EventService eventService, RegistrationService registrationService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
    }
    
    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents() {
        List<EventDTO> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id) {
        EventDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            Authentication authentication
    ) {
        User loggedInOrganizer = (User) authentication.getPrincipal();

        EventDTO createdEvent = eventService.createEvent(request, loggedInOrganizer);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }
    @PostMapping("/{id}/register")
    public ResponseEntity<MessageResponse> registerForEvent(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User loggedInUser = (User) authentication.getPrincipal();

        registrationService.registerForEvent(id, loggedInUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MessageResponse("Registration successful! Check your email for confirmation."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody CreateEventRequest request,
            Authentication authentication
    ) {
        User loggedInOrganizer = (User) authentication.getPrincipal();

        EventDTO updatedEvent = eventService.updateEvent(id, request, loggedInOrganizer);

        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User loggedInOrganizer = (User) authentication.getPrincipal();
        eventService.deleteEvent(id, loggedInOrganizer);
        return ResponseEntity.noContent().build();
    }
}
