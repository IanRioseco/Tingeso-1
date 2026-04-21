import Keycloak from 'keycloak-js';

// Instancia principal de Keycloak usada por toda la app.
// Toma valores desde variables Vite para evitar hardcodear ambientes.
const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:9090',
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'sisgr-realm',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'sisgr-frontend',
});

export default keycloak;