import React, { useState, useEffect } from 'react';
import eventService from '../services/eventService';
import EventCard from '../components/EventCard';

const Homepage = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        setLoading(true);
        const response = await eventService.getAllEvents();
        setEvents(response.data);
        setError(null);
      } catch (err) {
        setError('Failed to fetch events. Is the backend running?');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchEvents();
  }, []);

  if (loading) return <div style={{textAlign:'center', padding:'50px'}}>Loading awesome events...</div>;
  if (error) return <div className="error-message">{error}</div>;

  return (
    <div>
      <div className="hero-section">
        <h1 className="hero-title">Discover Local Events</h1>
        <p className="hero-subtitle">Join the community, learn something new, or just have fun.</p>
      </div>

      <h2 style={{marginBottom: '30px'}}>Upcoming Events</h2>
      
      <div className="event-grid">
        {events.length > 0 ? (
          events.map((event) => <EventCard key={event.id} event={event} />)
        ) : (
          <div style={{textAlign:'center', width:'100%'}}>
            <h3>No events found right now.</h3>
            <p>Check back later!</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Homepage;