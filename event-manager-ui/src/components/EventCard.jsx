import React from 'react';
import { Link } from 'react-router-dom';

const EventCard = ({ event, isDashboard = false }) => {
  const eventDate = new Date(event.eventDate).toLocaleString();

  const detailUrl = isDashboard 
    ? `/dashboard/event/${event.id}` 
    : `/event/${event.id}`;

  return (
    <div className="event-card">
      <h3>{event.title}</h3>
      <p>
        <strong>When:</strong> {eventDate}
      </p>
      <p>
        <strong>Where:</strong> {event.location}
      </p>
      <p>
        <strong>Spots Left:</strong> {event.availableSpots}
      </p>
      <p>
        <em>Organizer: {event.organizer.username}</em>
      </p>
      
      
      <Link to={detailUrl} className="btn-details">
        {isDashboard ? 'View Details & Attendees' : 'View Details'}
      </Link>
    </div>
  );
};

export default EventCard;