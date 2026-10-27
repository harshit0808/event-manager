import React, { useState, useEffect } from 'react';
import registrationService from '../services/registrationService';
import { QRCodeSVG } from 'qrcode.react';

const MyTicketsPage = () => {
  const [upcomingRegistrations, setUpcomingRegistrations] = useState([]);
  const [pastRegistrations, setPastRegistrations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchTickets = async () => {
      try {
        setLoading(true);
        const response = await registrationService.getMyRegistrations();
        
        const now = new Date();
        const upcoming = [];
        const past = [];

        response.data.forEach(reg => {
          if (new Date(reg.eventDate) > now) {
            upcoming.push(reg);
          } else {
            past.push(reg);
          }
        });

        setUpcomingRegistrations(upcoming);
        setPastRegistrations(past);
        
        setError(null);
      } catch (err) {
        setError('Failed to fetch your tickets.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchTickets();
  }, []);

  if (loading) return <div>Loading your tickets...</div>;
  if (error) return <div className="error-message">{error}</div>;

  return (
    <div>
      <h2>My Upcoming Tickets</h2>
      {upcomingRegistrations.length > 0 ? (
        <div className="event-grid">
          {upcomingRegistrations.map((reg) => (
            <div className="event-card" key={reg.registrationId}>
              <h3>{reg.eventTitle}</h3>
              <p>
                <strong>Ticket #{reg.registrationId}</strong>
              </p>
              <p>
                <strong>When:</strong> {new Date(reg.eventDate).toLocaleString()}
              </p>
              <p>
                <strong>Where:</strong> {reg.eventLocation}
              </p>
              
              <div style={{ background: 'white', padding: '16px', marginTop: '10px', textAlign: 'center' }}>
                <QRCodeSVG value={reg.qrCodeData} size={200} />
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p>You have not registered for any upcoming events yet.</p>
      )}

      <hr style={{margin: '40px 0'}} />
      <h2>My Past Events</h2>
      {pastRegistrations.length > 0 ? (
        <div className="event-grid">
          {pastRegistrations.map((reg) => (
            <div className="event-card" key={reg.registrationId}>
              <h3>{reg.eventTitle}</h3>
              <p>
                <strong>Ticket #{reg.registrationId}</strong>
              </p>
              <p>
                <strong>When:</strong> {new Date(reg.eventDate).toLocaleString()}
              </p>
              <p>
                <strong>Where:</strong> {reg.eventLocation}
              </p>
              <p style={{marginTop: '15px', fontWeight: 500}}><em>This event has ended.</em></p>
            </div>
          ))}
        </div>
      ) : (
        <p>You have no past event registrations.</p>
      )}
    </div>
  );
};

export default MyTicketsPage;