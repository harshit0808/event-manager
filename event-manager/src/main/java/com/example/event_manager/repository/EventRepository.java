package com.example.event_manager.repository;

import com.example.event_manager.domain.Event;
import com.example.event_manager.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // The organizer is always needed to build an EventDTO, so fetch it in the same query
    // instead of issuing one extra query per event (N+1 problem).
    @Override
    @EntityGraph(attributePaths = "organizer")
    List<Event> findAll();

    @Override
    @EntityGraph(attributePaths = "organizer")
    Optional<Event> findById(Long id);

    @EntityGraph(attributePaths = "organizer")
    List<Event> findByOrganizer(User organizer);

    /**
     * Atomically takes one spot. The WHERE clause makes the database enforce
     * "spots > 0", so two concurrent registrations can never oversell the last spot.
     *
     * @return 1 if a spot was taken, 0 if the event is full (or does not exist)
     */
    @Modifying
    @Query("update Event e set e.availableSpots = e.availableSpots - 1 "
            + "where e.id = :id and e.availableSpots > 0")
    int decrementAvailableSpots(@Param("id") Long id);
}
