import React, { useState } from 'react';
import authService from '../services/authService';
import { useNavigate } from 'react-router-dom';

const RegisterPage = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('ROLE_USER');
  const [organizerCode, setOrganizerCode] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      await authService.register({
        username,
        email,
        password,
        role,
        organizerCode: role === 'ROLE_ORGANIZER' ? organizerCode : undefined,
      });
      navigate('/login');
      
    } catch (err) {
      if (err.response && err.response.data && err.response.data.message) {
        setError(err.response.data.message);
      } else {
        setError('Registration failed. Please try again.');
      }
      console.error(err);
    }
  };

  return (
    <div className="form-container">
      <h2>Register</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Username:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Email:</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Password:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Register as:</label>
          <select value={role} onChange={(e) => setRole(e.target.value)}>
            <option value="ROLE_USER">Attendee</option>
            <option value="ROLE_ORGANIZER">Event Organizer</option>
          </select>
        </div>
        {role === 'ROLE_ORGANIZER' && (
          <div className="form-group">
            <label>Organizer invite code:</label>
            <input
              type="password"
              value={organizerCode}
              onChange={(e) => setOrganizerCode(e.target.value)}
              required
            />
          </div>
        )}
        {error && <p className="error-message">{error}</p>} 
        <button type="submit" className="btn-submit">Register</button>
      </form>
    </div>
  );
};

export default RegisterPage;