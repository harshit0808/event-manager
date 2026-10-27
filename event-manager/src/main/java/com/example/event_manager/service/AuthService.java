package com.example.event_manager.service;

import com.example.event_manager.domain.Role;
import com.example.event_manager.domain.User;
import com.example.event_manager.dto.AuthResponse;
import com.example.event_manager.dto.LoginRequest;
import com.example.event_manager.dto.RegisterRequest;
import com.example.event_manager.exception.ConflictException;
import com.example.event_manager.repository.UserRepository;
import com.example.event_manager.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final String organizerInviteCode;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       @Value("${app.organizer.invite-code:}") String organizerInviteCode) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.organizerInviteCode = organizerInviteCode;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already in use!");
        }

        // Never trust the role sent by the client: becoming an organizer needs a secret invite code.
        if (request.getRole() == Role.ROLE_ORGANIZER && !isValidOrganizerCode(request.getOrganizerCode())) {
            throw new AccessDeniedException("A valid organizer invite code is required to register as an organizer.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder().token(jwtToken).build();
    }

    public AuthResponse login(LoginRequest request) {
        // Throws BadCredentialsException (-> 401 via GlobalExceptionHandler) on wrong credentials.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder().token(jwtToken).build();
    }

    private boolean isValidOrganizerCode(String provided) {
        if (organizerInviteCode == null || organizerInviteCode.isBlank() || provided == null) {
            return false; // organizer sign-up is disabled until an invite code is configured
        }
        // constant-time comparison
        return MessageDigest.isEqual(
                organizerInviteCode.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }
}
