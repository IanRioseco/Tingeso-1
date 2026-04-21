import axios from 'axios';
import keycloak from './keycloak';

// Cliente HTTP único del frontend.
// Centraliza URL base e inyección automática del token en cada request.
const apiClient = axios.create({
  // En dev conviene usar '/api' para aprovechar el proxy de Vite y evitar CORS.
  // Si defines VITE_API_BASE_URL, ese valor tiene prioridad.
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
});

apiClient.interceptors.request.use(
  async (config) => {
    // Si hay sesión, intenta refrescar token antes de enviar la petición.
    if (keycloak.authenticated) {
      try {
        await keycloak.updateToken(30);
      } catch {
        // Si falla el refresh, se usa el token almacenado para no bloquear la request.
      }
    }

    // Usa el token vigente de Keycloak o, como respaldo, el almacenado localmente.
    const token = keycloak.token || localStorage.getItem('kc_token');
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

export default apiClient;