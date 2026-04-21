// src/services/userService.js
import api from './http-common';

// Encapsula endpoints de gestión de usuarios.
const userService = {
  register: (data) => api.post('/users/register', data),
  getById: (id) => api.get(`/users/${id}`),
  getAll: () => api.get('/users'),
  update: (id, data) => api.put(`/users/${id}`, data),
  deactivate: (id) => api.delete(`/users/${id}`),
};

export default userService;