// src/services/reportService.js
import api from './http-common';

// Agrupa endpoints de reportería para el panel administrativo.
const reportService = {
  getSales: (from, to) => api.get('/reports/sales', { params: { from, to } }),
  getRanking: (from, to) => api.get('/reports/ranking', { params: { from, to } }),
};

export default reportService;