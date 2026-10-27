package com.example.event_manager.service;

import com.example.event_manager.domain.Event;
import com.example.event_manager.domain.Registration;
import com.example.event_manager.domain.User;
import com.example.event_manager.dto.RegistrationDTO;
import com.example.event_manager.exception.ConflictException;
import com.example.event_manager.exception.ResourceNotFoundException;
import com.example.event_manager.repository.EventRepository;
import com.example.event_manager.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final QRCodeService qrCodeService;
    private final EmailService emailService;

    public RegistrationService(RegistrationRepository registrationRepository,
                               EventRepository eventRepository,
                               QRCodeService qrCodeService,
                               EmailService emailService) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.qrCodeService = qrCodeService;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public List<RegistrationDTO> getMyRegistrations(User user) {
        return registrationRepository.findByUser(user).stream()
                .map(this::convertRegistrationToDto)
                .toList();
    }

    private RegistrationDTO convertRegistrationToDto(Registration reg) {
        return RegistrationDTO.builder()
                .registrationId(reg.getId())
                .qrCodeData(reg.getQrCodeData())
                .registrationDate(reg.getRegistrationDate())
                .eventTitle(reg.getEvent().getTitle())
                .eventLocation(reg.getEvent().getLocation())
                .eventDate(reg.getEvent().getEventDate())
                .build();
    }

    @Transactional
    public void registerForEvent(Long eventId, User user) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        if (registrationRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            throw new ConflictException("You are already registered for this event.");
        }

        // Atomic "take one spot" - safe when many users register at the same moment.
        if (eventRepository.decrementAvailableSpots(eventId) == 0) {
            throw new ConflictException("This event is full. No available spots.");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // The ticket code is a random UUID, so tickets cannot be guessed or forged.
        Registration registration = Registration.builder()
                .user(user)
                .event(event)
                .registrationDate(LocalDateTime.now())
                .qrCodeData(UUID.randomUUID().toString())
                .build();
        Registration saved = registrationRepository.save(registration);

        byte[] qrCodeBytes = qrCodeService.generateQRCode(saved.getQrCodeData(), 250, 250);

        sendEmailAfterCommit(user.getEmail(), event.getTitle(), qrCodeBytes);
    }

    /** Only send the email once the registration is really saved (not if the transaction rolls back). */
    private void sendEmailAfterCommit(String toEmail, String eventTitle, byte[] qrCodeBytes) {
        Runnable send = () -> emailService.sendRegistrationConfirmation(toEmail, eventTitle, qrCodeBytes);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send.run();
                }
            });
        } else {
            send.run();
        }
    }
}
