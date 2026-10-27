import React, { useState, useEffect } from 'react';
import eventService from '../services/eventService';
import EventCard from '../components/EventCard';

const OrganizerDashboard = () => {
  const [myEvents, setMyEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    location: '',
    eventDate: '',
    availableSpots: 10,
  });

  const fetchMyEvents = async () => {
    try {
      setLoading(true);
      const response = await eventService.getMyEvents();
      setMyEvents(response.data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch your events. Are you logged in?');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyEvents();
  }, []);

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    try {
      await eventService.createEvent(formData);
      setFormData({
        title: '',
        description: '',
        location: '',
        eventDate: '',
        availableSpots: 10,
      });
      fetchMyEvents();
    } catch (err) {
      setError('Failed to create event. Check your data.');
      console.error(err);
    }
  };

  return (
    <div>
      <h2>Organizer Dashboard</h2>
      {error && <p className="error-message">{error}</p>}
      
      <div className="dashboard">
        <div className="form-container dashboard-form">
          <h3>Create New Event</h3>
          <form onSubmit={handleFormSubmit}>
            <div className="form-group">
              <label>Title:</label>
              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleFormChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Description:</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleFormChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Location:</label>
              <input
                type="text"
                name="location"
                value={formData.location}
                onChange={handleFormChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Date and Time:</label>
              <input
                type="datetime-local"
                name="eventDate"
                value={formData.eventDate}
                onChange={handleFormChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Available Spots:</label>
              <input
                type="number"
                name="availableSpots"
                value={formData.availableSpots}
                onChange={handleFormChange}
                min="1"
                required
              />
            </div>
            <button type="submit" className="btn-submit">Create Event</button>
          </form>
        </div>

        <div className="dashboard-list">
          <h3>My Events</h3>
          {loading ? (
            <p>Loading your events...</p>
          ) : (
            <div className="event-grid">
              {myEvents.length > 0 ? (
                myEvents.map((event) => (
                  <EventCard 
                  key={event.id} 
                  event={event} 
                  isDashboard={true} 
                />
                ))
              ) : (
                <p>You have not created any events yet.</p>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default OrganizerDashboard;