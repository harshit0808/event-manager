import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Navigate, useLocation } from 'react-router-dom';

const ProtectedRoute = ({ children }) => {
  const { user } = useAuth();
  const location = useLocation();

  if (user && user.role === 'ROLE_ORGANIZER') {
    return children;
  }
  return <Navigate to="/login" state={{ from: location }} replace />;
};

export default ProtectedRoute;