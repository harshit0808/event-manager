import React, { createContext, useState, useContext, useEffect } from 'react';
import authService from '../services/authService';
import { jwtDecode } from 'jwt-decode'; 

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null); 
  const [token, setToken] = useState(localStorage.getItem('token')); 

  useEffect(() => {
    if (token) {
      try {
        const decodedToken = jwtDecode(token);
        
        const isExpired = decodedToken.exp * 1000 < Date.now();
        
        if (isExpired) {
          logout(); 
        } else {
          setUser({
            username: decodedToken.sub, 
            role: decodedToken.authorities[0].authority, 
          });
        }
      } catch (error) {
        console.error('Invalid token:', error);
        logout();
      }
    }
  }, [token]);

  const login = async (username, password) => {
    try {
      const response = await authService.login({ username, password });
      const { token } = response.data;
      
      localStorage.setItem('token', token); 
      setToken(token);
      
      const decodedToken = jwtDecode(token);
      setUser({
        username: decodedToken.sub,
        role: decodedToken.authorities[0].authority,
      });
      return true;
    } catch (error) {
      console.error('Login failed:', error);
      return false;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
    setToken(null);
  };
  
  const value = {
    user,
    token,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => {
  return useContext(AuthContext);
};