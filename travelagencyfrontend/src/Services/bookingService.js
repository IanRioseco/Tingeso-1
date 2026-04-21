// src/services/bookingService.js
import api from './http-common';

// Encapsula todas las operaciones HTTP relacionadas con reservas.
const bookingService = {
  create: (data) => api.post('/bookings', data),
  getById: (id) => api.get(`/bookings/${id}`),
  getMyBookings: () => api.get('/bookings/my'),
  getAll: () => api.get('/bookings'),
  cancel: (id) => api.patch(`/bookings/${id}/cancel`),
};

export default bookingService;