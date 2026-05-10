import Keycloak from 'keycloak-js';

// Instancia principal de Keycloak usando HTTPS para evitar mixed content.
const keycloakUrl = 'https://3.12.42.217';
console.log('[DEBUG] keycloak.js: Using hardcoded HTTPS URL:', keycloakUrl);

const keycloak = new Keycloak({
  url: keycloakUrl,
  realm: 'sisgr-realm',
  clientId: 'sisgr-frontend',
});

console.log('[DEBUG] keycloak.js: Keycloak instance created with:', { url: keycloakUrl, realm: keycloak.realm, clientId: keycloak.clientId });

export default keycloak;