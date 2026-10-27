import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import eventService from '../services/eventService';
import { useAuth } from '../context/AuthContext';

const EventDetailsPage = () => {
  const { id } = useParams();
  const { user } = useAuth();
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [message, setMessage] = useState('');

  useEffect(() => {
    const fetchEvent = async () => {
      try {
        setLoading(true);
        const response = await eventService.getEventById(id);
        setEvent(response.data);
        setError(null);
      } catch (err) {
        setError('Failed to fetch event details.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchEvent();
  }, [id]);

  const handleRegister = async () => {
    setMessage('');
    try {
      const response = await eventService.registerForEvent(id);
      setMessage(response.data.message);
      setEvent(prevEvent => ({
        ...prevEvent,
        availableSpots: prevEvent.availableSpots - 1
      }));
    } catch (err) {
      setMessage(`Error: ${err.response?.data?.message || err.message}`);
      console.error(err);
    }
  };

  if (loading) return <div>Loading event...</div>;
  if (error) return <div className="error-message">{error}</div>;
  if (!event) return <div>Event not found.</div>;

  const isSoldOut = event.availableSpots <= 0;

  return (
    <div className="event-details">
      <h2>{event.title}</h2>
      <p>
        <strong>Organized by:</strong> {event.organizer.username}
      </p>
      <p>
        <strong>When:</strong> {new Date(event.eventDate).toLocaleString()}
      </p>
      <p>
        <strong>Where:</strong> {event.location}
      </p>
      <p>
        <strong>Spots Left:</strong> {event.availableSpots}
      </p>
      <hr />
      <h3>Details</h3>
      <p>{event.description}</p>

      {user && (
    <div>
      <button
        className={isSoldOut ? "" : "btn-register"} 
        onClick={handleRegister}
        disabled={isSoldOut}
      >
        {isSoldOut ? 'Sold Out' : 'Register for this Event'}
      </button>
      
      {message && <p className={message.startsWith('Error:') ? 'error-message' : 'success-message'}>{message}</p>}
    </div>
    )}
      
      {!user && (
        <p><strong>Please log in to register for this event.</strong></p>
      )}
    </div>
  );
};

export default EventDetailsPage;