import Keycloak from 'keycloak-js';

// Instancia principal de Keycloak usando HTTP (diagnóstico de HTTPS/SSL issues)
const keycloakUrl = 'http://3.12.42.217:8180';
console.log('[DEBUG] keycloak.js: Using hardcoded HTTP URL:', keycloakUrl);

const keycloak = new Keycloak({
  url: keycloakUrl,
  realm: 'sisgr-realm',
  clientId: 'sisgr-frontend',
});

console.log('[DEBUG] keycloak.js: Keycloak instance created with:', { url: keycloakUrl, realm: keycloak.realm, clientId: keycloak.clientId });

export default keycloak;