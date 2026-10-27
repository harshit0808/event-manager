import api from './api';

const eventService = {
  getAllEvents: () => api.get('/api/events'),

  getEventById: (id) => api.get(`/api/events/${id}`),

  createEvent: (eventData) => api.post('/api/events', eventData),

  updateEvent: (id, eventData) => api.put(`/api/events/${id}`, eventData),

  deleteEvent: (id) => api.delete(`/api/events/${id}`),

  registerForEvent: (id) => api.post(`/api/events/${id}/register`),

  getMyEvents: () => api.get('/api/organizer/my-events'),

  getEventAttendees: (id) => api.get(`/api/organizer/my-events/${id}/attendees`),

  checkInAttendee: (id, qrCodeData) =>
    api.post(`/api/organizer/my-events/${id}/check-in`, { qrCodeData }),
};

export default eventService;
