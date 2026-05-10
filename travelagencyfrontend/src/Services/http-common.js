import axios from 'axios';
import keycloak from './keycloak';

const envBaseUrl = import.meta.env.VITE_API_BASE_URL;
const isBrowser = typeof window !== 'undefined';
const PROD_DEFAULT_API = 'https://3.14.14.199/api';
const isPublicHost = isBrowser && !['localhost', '127.0.0.1'].includes(window.location.hostname);
const isLocalEnvBase = typeof envBaseUrl === 'string' && /https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/i.test(envBaseUrl);
// Si estamos en un host público pero la variable apunta a localhost, usar ruta relativa '/api'
const resolvedBaseUrl = isPublicHost && isLocalEnvBase ? '/api' : (envBaseUrl || PROD_DEFAULT_API);

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