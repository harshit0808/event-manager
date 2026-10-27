import api from './api';

const authService = {
  login: (loginData) => api.post('/auth/login', loginData),
  register: (registerData) => api.post('/auth/register', registerData),
};

export default authService;
