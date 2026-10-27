import api from './api';

const registrationService = {
  getMyRegistrations: () => api.get('/api/my-registrations'),
};

export default registrationService;
