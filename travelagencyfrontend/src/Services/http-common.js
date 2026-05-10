import axios from 'axios';
import keycloak from './keycloak';

const envBaseUrl = import.meta.env.VITE_API_BASE_URL;
const isBrowser = typeof window !== 'undefined';
const isPublicHost = isBrowser && !['localhost', '127.0.0.1'].includes(window.location.hostname);
const isLocalEnvBase = typeof envBaseUrl === 'string' && /https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/i.test(envBaseUrl);
const resolvedBaseUrl = isPublicHost && isLocalEnvBase ? '/api' : (envBaseUrl || '/api');

// Cliente HTTP único del frontend.
// Centraliza URL base e inyección automática del token en cada request.
const apiClient = axios.create({
  // En despliegue público no debe usar localhost aunque venga por variable de build.
  baseURL: resolvedBaseUrl,
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

    // Usa solo el token vigente de Keycloak para evitar enviar credenciales obsoletas.
    const token = keycloak.authenticated ? keycloak.token : null;
    if (!keycloak.authenticated) {
      localStorage.removeItem('kc_token');
      localStorage.removeItem('kc_refresh_token');
      localStorage.removeItem('kc_id_token');
    }
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

export default apiClient;