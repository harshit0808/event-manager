import axios from 'axios';

// Set VITE_API_URL in .env (see .env.example) when the backend is not on localhost:8080.
export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({ baseURL: API_BASE_URL });

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    // Login/register must never carry a (possibly expired) token.
    if (token && !config.url.startsWith('/auth')) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default api;
