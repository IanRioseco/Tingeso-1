// src/services/paymentService.js
import api from './http-common';

// Expone llamadas de pago y consulta de pago por reserva.
const paymentService = {
  pay: (bookingId, data) => api.post(`/payments/${bookingId}`, data),
  getByBooking: (bookingId) => api.get(`/payments/booking/${bookingId}`),
};

export default paymentService;