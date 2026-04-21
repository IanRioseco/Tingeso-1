// src/services/packageService.js
import api from './http-common';

// Encapsula operaciones CRUD y búsqueda de paquetes turísticos.
const packageService = {
  getAll: () => api.get('/packages'),
  getAvailable: () => api.get('/packages/available'),
  getById: (id) => api.get(`/packages/${id}`),
  search: (params) => api.get('/packages/search', { params }),
  create: (data) => api.post('/packages', data),
  update: (id, data) => api.put(`/packages/${id}`, data),
  changeStatus: (id, status) => api.patch(`/packages/${id}/status`, null, { params: { status } }),
  delete: (id) => api.delete(`/packages/${id}`),
};

export default packageService;