import Keycloak from 'keycloak-js';

// Instancia principal de Keycloak usada por toda la app.
// Toma valores desde variables Vite; si no están definidas, usa el origen
// actual del navegador y como último recurso usa el dominio de producción.
const PROD_DEFAULT = 'https://3.14.14.199';
const computedOrigin = (typeof window !== 'undefined' && window.location && window.location.origin)
  ? window.location.origin
  : PROD_DEFAULT;

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL || computedOrigin,
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'sisgr-realm',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'sisgr-frontend',
});

export default keycloak;