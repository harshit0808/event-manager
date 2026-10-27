package com.example.event_manager.service;

import com.example.event_manager.domain.Event;
import com.example.event_manager.domain.Registration;
import com.example.event_manager.domain.User;
import com.example.event_manager.dto.AttendeeDTO;
import com.example.event_manager.dto.CreateEventRequest;
import com.example.event_manager.dto.EventDTO;
import com.example.event_manager.dto.OrganizerDTO;
import com.example.event_manager.exception.ConflictException;
import com.example.event_manager.exception.ResourceNotFoundException;
import com.example.event_manager.repository.EventRepository;
import com.example.event_manager.repository.RegistrationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    public EventService(EventRepository eventRepository, RegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional(readOnly = true)
    public List<EventDTO> getAllEvents() {
        return eventRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Transactional(readOnly = true)
    public EventDTO getEventById(Long id) {
        return convertToDto(findEvent(id));
    }

    @Transactional(readOnly = true)
    public List<EventDTO> getEventsForOrganizer(User organizer) {
        return eventRepository.findByOrganizer(organizer).stream().map(this::convertToDto).toList();
    }

    @Transactional
    public EventDTO createEvent(CreateEventRequest request, User organizer) {
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .eventDate(request.getEventDate())
                .availableSpots(request.getAvailableSpots())
                .organizer(organizer)
                .build();

        return convertToDto(eventRepository.save(event));
    }

    @Transactional
    public EventDTO updateEvent(Long eventId, CreateEventRequest request, User loggedInOrganizer) {
        Event event = findOwnedEvent(eventId, loggedInOrganizer);

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setEventDate(request.getEventDate());
        event.setAvailableSpots(request.getAvailableSpots());

        return convertToDto(eventRepository.save(event));
    }

    @Transactional
    public void deleteEvent(Long eventId, User loggedInOrganizer) {
        Event event = findOwnedEvent(eventId, loggedInOrganizer);
        eventRepository.delete(event); // registrations are removed too (cascade)
    }

    @Transactional(readOnly = true)
    public List<AttendeeDTO> getEventAttendees(Long eventId, User loggedInOrganizer) {
        findOwnedEvent(eventId, loggedInOrganizer);
        return registrationRepository.findByEventId(eventId).stream()
                .map(this::convertRegistrationToAttendeeDto)
                .toList();
    }

    /** Marks a ticket as used. Called by the organizer when scanning an attendee's QR code. */
    @Transactional
    public AttendeeDTO checkInAttendee(Long eventId, String qrCodeData, User loggedInOrganizer) {
        findOwnedEvent(eventId, loggedInOrganizer);

        Registration registration = registrationRepository.findByQrCodeData(qrCodeData.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found."));

        if (!registration.getEvent().getId().equals(eventId)) {
            throw new ConflictException("This ticket belongs to a different event.");
        }
        if (registration.getCheckedInAt() != null) {
            throw new ConflictException("This ticket was already used (checked in).");
        }

        registration.setCheckedInAt(LocalDateTime.now());
        return convertRegistrationToAttendeeDto(registrationRepository.save(registration));
    }

    private Event findEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    private Event findOwnedEvent(Long eventId, User organizer) {
        Event event = findEvent(eventId);
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new AccessDeniedException("You are not the organizer of this event.");
        }
        return event;
    }

    private AttendeeDTO convertRegistrationToAttendeeDto(Registration registration) {
        return AttendeeDTO.builder()
                .registrationId(registration.getId())
                .username(registration.getUser().getUsername())
                .email(registration.getUser().getEmail())
                .registrationDate(registration.getRegistrationDate())
                .checkedIn(registration.getCheckedInAt() != null)
                .checkedInAt(registration.getCheckedInAt())
                .build();
    }

    private EventDTO convertToDto(Event event) {
        OrganizerDTO organizerDto = OrganizerDTO.builder()
                .id(event.getOrganizer().getId())
                .username(event.getOrganizer().getUsername())
                .build();

        return EventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .eventDate(event.getEventDate())
                .availableSpots(event.getAvailableSpots())
                .organizer(organizerDto)
                .build();
    }
}
