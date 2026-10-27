package com.example.event_manager.repository;

import com.example.event_manager.domain.Registration;
import com.example.event_manager.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @EntityGraph(attributePaths = "event")
    List<Registration> findByUser(User user);

    @EntityGraph(attributePaths = "user")
    List<Registration> findByEventId(Long eventId);

    Optional<Registration> findByQrCodeData(String qrCodeData);
}
