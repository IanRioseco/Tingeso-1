import Keycloak from 'keycloak-js';

// Instancia principal de Keycloak usada por toda la app.
// Toma valores desde variables Vite o construye una URL por defecto
// basada en el origen actual para evitar depender de "localhost" hardcodeado.
const defaultOrigin = typeof window !== 'undefined'
  ? `${window.location.protocol}//${window.location.hostname}${window.location.port ? ':' + window.location.port : ''}`
  : 'http://localhost:9090';

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL || defaultOrigin,
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'sisgr-realm',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'sisgr-frontend',
});

export default keycloak;