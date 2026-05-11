// src/services/userService.js
import api from './http-common';

// Encapsula endpoints de gestión de usuarios.
// Los usuarios se mantienen en Keycloak 
// y se sincronizan con el sistema.
const userService = {
  register: (data) => api.post('/users/register', data),
  me: () => api.get('/users/me'),
  updateMe: (data) => api.put('/users/me', data),
  getById: (id) => api.get(`/users/${id}`),
  getAll: () => api.get('/users'),
  update: (id, data) => api.put(`/users/${id}`, data),
  deactivate: (id) => api.delete(`/users/${id}`),
  reactivate: (id) => api.patch(`/users/${id}/reactivate`),
};

export default userService;