import Keycloak from 'keycloak-js';

// Instancia principal de Keycloak usada por toda la app.
// Toma valores desde variables Vite o construye una URL por defecto
// basada en el origen actual para evitar depender de "localhost" hardcodeado.
const defaultOrigin = typeof window !== 'undefined'
  ? `${window.location.protocol}//${window.location.hostname}${window.location.port ? ':' + window.location.port : ''}`
  : 'http://localhost:9090';

const keycloakUrl = import.meta.env.VITE_KEYCLOAK_URL || defaultOrigin;
console.log('[DEBUG] keycloak.js: VITE_KEYCLOAK_URL =', import.meta.env.VITE_KEYCLOAK_URL);
console.log('[DEBUG] keycloak.js: defaultOrigin =', defaultOrigin);
console.log('[DEBUG] keycloak.js: final keycloakUrl =', keycloakUrl);

const keycloak = new Keycloak({
  url: keycloakUrl,
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'sisgr-realm',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'sisgr-frontend',
});

console.log('[DEBUG] keycloak.js: Keycloak instance created with:', { url: keycloakUrl, realm: keycloak.realm, clientId: keycloak.clientId });

export default keycloak;