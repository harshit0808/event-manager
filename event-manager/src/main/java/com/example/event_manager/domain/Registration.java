package com.example.event_manager.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; 
import lombok.ToString; 

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "registrations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "event_id"})
        })
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime registrationDate;

    @Column(name = "qr_code_data")
    private String qrCodeData;

    /** Set when the organizer scans the ticket at the door; null means not checked in yet. */
    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude 
    @EqualsAndHashCode.Exclude 
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude 
    @EqualsAndHashCode.Exclude 
    private Event event;

}