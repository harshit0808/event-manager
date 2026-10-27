import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import eventService from '../services/eventService';

// --- Styles ---
const attendeeTableStyle = {
  width: '100%',
  marginTop: '20px',
  borderCollapse: 'collapse',
};
const thStyle = {
  border: '1px solid #ddd',
  padding: '12px',
  backgroundColor: '#f9f9f9',
  textAlign: 'left',
};
const tdStyle = {
  border: '1px solid #ddd',
  padding: '12px',
};

// <input type="datetime-local"> needs "YYYY-MM-DDTHH:mm"
const toInputDateTime = (value) => (value ? String(value).slice(0, 16) : '');

const errorMessage = (err, fallback) => err.response?.data?.message || fallback;

const OrganizerEventDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [event, setEvent] = useState(null);
  const [attendees, setAttendees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // edit state
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState(null);
  const [formMessage, setFormMessage] = useState(null);

  // check-in state
  const [ticketCode, setTicketCode] = useState('');
  const [checkInMessage, setCheckInMessage] = useState(null);

  const fetchData = useCallback(async () => {
    try {
      setLoading(true);
      const [eventResponse, attendeesResponse] = await Promise.all([
        eventService.getEventById(id),
        eventService.getEventAttendees(id),
      ]);
      setEvent(eventResponse.data);
      setAttendees(attendeesResponse.data);
      setError(null);
    } catch (err) {
      setError(errorMessage(err, 'Failed to load event data. Are you the organizer?'));
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const startEditing = () => {
    setFormData({
      title: event.title,
      description: event.description,
      location: event.location,
      eventDate: toInputDateTime(event.eventDate),
      availableSpots: event.availableSpots,
    });
    setFormMessage(null);
    setIsEditing(true);
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    try {
      const response = await eventService.updateEvent(id, formData);
      setEvent(response.data);
      setIsEditing(false);
      setFormMessage({ type: 'success', text: 'Event updated.' });
    } catch (err) {
      setFormMessage({ type: 'error', text: errorMessage(err, 'Failed to update the event.') });
    }
  };

  const handleDelete = async () => {
    const confirmed = window.confirm(
      `Delete "${event.title}"? This also removes all ${attendees.length} registration(s) and cannot be undone.`
    );
    if (!confirmed) return;
    try {
      await eventService.deleteEvent(id);
      navigate('/dashboard');
    } catch (err) {
      setFormMessage({ type: 'error', text: errorMessage(err, 'Failed to delete the event.') });
    }
  };

  const handleCheckIn = async (e) => {
    e.preventDefault();
    setCheckInMessage(null);
    try {
      const response = await eventService.checkInAttendee(id, ticketCode);
      setCheckInMessage({ type: 'success', text: `Checked in ${response.data.username}.` });
      setTicketCode('');
      fetchData();
    } catch (err) {
      setCheckInMessage({ type: 'error', text: errorMessage(err, 'Check-in failed.') });
    }
  };

  if (loading) return <div>Loading event details...</div>;
  if (error) return <div className="error-message">{error}</div>;
  if (!event) return <div>Event not found.</div>;

  const checkedInCount = attendees.filter((a) => a.checkedIn).length;

  return (
    <div className="event-details">
      {isEditing ? (
        <form onSubmit={handleSave} className="form-container">
          <h3>Edit Event</h3>
          <div className="form-group">
            <label>Title:</label>
            <input name="title" value={formData.title} onChange={handleFormChange} required />
          </div>
          <div className="form-group">
            <label>Description:</label>
            <textarea name="description" value={formData.description} onChange={handleFormChange} required />
          </div>
          <div className="form-group">
            <label>Location:</label>
            <input name="location" value={formData.location} onChange={handleFormChange} required />
          </div>
          <div className="form-group">
            <label>Date and Time:</label>
            <input type="datetime-local" name="eventDate" value={formData.eventDate} onChange={handleFormChange} required />
          </div>
          <div className="form-group">
            <label>Spots Left:</label>
            <input type="number" name="availableSpots" min="1" value={formData.availableSpots} onChange={handleFormChange} required />
          </div>
          <button type="submit" className="btn-submit">Save changes</button>{' '}
          <button type="button" onClick={() => setIsEditing(false)}>Cancel</button>
        </form>
      ) : (
        <>
          <h2>{event.title}</h2>
          <p>{event.description}</p>
          <p>
            <strong>When:</strong> {new Date(event.eventDate).toLocaleString()}
          </p>
          <p>
            <strong>Where:</strong> {event.location}
          </p>
          <p>
            <strong>Total Spots:</strong> {event.availableSpots + attendees.length}
          </p>
          <p>
            <strong>Spots Left:</strong> {event.availableSpots}
          </p>
          <p>
            <strong>Total Registrations:</strong> {attendees.length} ({checkedInCount} checked in)
          </p>
          <button onClick={startEditing}>Edit event</button>{' '}
          <button onClick={handleDelete} className="btn-logout">Delete event</button>
        </>
      )}
      {formMessage && (
        <p className={formMessage.type === 'error' ? 'error-message' : 'success-message'}>{formMessage.text}</p>
      )}

      <hr style={{ margin: '30px 0' }} />
      <h3>Check in an attendee</h3>
      <p>Scan the attendee's QR code (or paste the ticket code) and press Check in.</p>
      <form onSubmit={handleCheckIn}>
        <input
          type="text"
          value={ticketCode}
          onChange={(e) => setTicketCode(e.target.value)}
          placeholder="Ticket code from the QR"
          required
          style={{ minWidth: '320px' }}
        />{' '}
        <button type="submit" className="btn-submit">Check in</button>
      </form>
      {checkInMessage && (
        <p className={checkInMessage.type === 'error' ? 'error-message' : 'success-message'}>{checkInMessage.text}</p>
      )}

      <hr style={{ margin: '30px 0' }} />
      <h3>Attendee List</h3>
      {attendees.length > 0 ? (
        <table style={attendeeTableStyle}>
          <thead>
            <tr>
              <th style={thStyle}>Ticket Number</th>
              <th style={thStyle}>Username</th>
              <th style={thStyle}>Email</th>
              <th style={thStyle}>Registered On</th>
              <th style={thStyle}>Checked in</th>
            </tr>
          </thead>
          <tbody>
            {attendees.map((attendee) => (
              <tr key={attendee.registrationId}>
                <td style={tdStyle}>#{attendee.registrationId}</td>
                <td style={tdStyle}>{attendee.username}</td>
                <td style={tdStyle}>{attendee.email}</td>
                <td style={tdStyle}>{new Date(attendee.registrationDate).toLocaleString()}</td>
                <td style={tdStyle}>
                  {attendee.checkedIn ? `Yes (${new Date(attendee.checkedInAt).toLocaleTimeString()})` : 'No'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <p>No attendees have registered yet.</p>
      )}
    </div>
  );
};

export default OrganizerEventDetailsPage;
