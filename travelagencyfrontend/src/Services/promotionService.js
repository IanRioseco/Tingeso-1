import api from './http-common';

// Encapsula operaciones CRUD y búsqueda de descuentos/promociones.
// Los descuentos/promociones se calculan 
// en función de los datos de los paquetes y de los clientes.
const promotionService = {
  calculate: (data) => api.post('/promotions/calculate', data),
  getActive: () => api.get('/promotions/active'),
  
  // Endpoints de Administrador
  getAll: () => api.get('/promotions'),
  create: (data) => api.post('/promotions', data),
  update: (id, data) => api.put(`/promotions/${id}`, data),
  changeStatus: (id, active) => api.patch(`/promotions/${id}/status?active=${active}`)
};

export default promotionService;