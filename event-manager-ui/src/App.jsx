import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Homepage from './pages/Homepage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import Navbar from './components/NavBar';
import OrganizerDashboard from './pages/OrganizerDashboard';
import ProtectedRoute from './components/ProtectedRoute';
import EventDetailsPage from './pages/EventDetailsPage';
import OrganizerEventDetailsPage from './pages/OrganizerEventDetailsPage';
import MyTicketsPage from './pages/MyTicketsPage';

function App() {
  return (
    <>
      <Navbar />
      <div className="app-container">
        <h1>Event Manager</h1>
        <Routes>
          <Route path="/" element={<Homepage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/event/:id" element={<EventDetailsPage />} />
          <Route path="/my-tickets" element={<MyTicketsPage />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <OrganizerDashboard />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/dashboard/event/:id"
            element={
              <ProtectedRoute>
                <OrganizerEventDetailsPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </div>
    </>
  );
}

export default App;