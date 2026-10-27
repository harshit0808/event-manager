package com.example.event_manager.controller;

import com.example.event_manager.domain.User;
import com.example.event_manager.dto.RegistrationDTO;
import com.example.event_manager.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/my-registrations")
    public ResponseEntity<List<RegistrationDTO>> getMyRegistrations(Authentication authentication) {
        User loggedInUser = (User) authentication.getPrincipal();
        List<RegistrationDTO> registrations = registrationService.getMyRegistrations(loggedInUser);
        return ResponseEntity.ok(registrations);
    }
}