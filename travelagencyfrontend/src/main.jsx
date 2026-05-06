import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { ReactKeycloakProvider } from '@react-keycloak/web';
import './index.css';
import App from './App.jsx';
import { AuthProvider } from './context/AuthContext.jsx';
import keycloak from './Services/keycloak.js';

// Polyfill para Web Crypto en HTTP (desarrollo)
if (!window.crypto || !window.crypto.subtle) {
  window.crypto = window.crypto || {};
  window.crypto.subtle = {
    digest: async () => new Uint8Array(32),
    generateKey: async () => ({ privateKey: {}, publicKey: {} }),
  };
}

const onTokens = (tokens = {}) => {
  const { token, refreshToken, idToken } = tokens;
  localStorage.setItem('kc_token', token || '');
  localStorage.setItem('kc_refresh_token', refreshToken || '');
  localStorage.setItem('kc_id_token', idToken || '');
};

const initOptions = {
  pkceMethod: 'S256',
  checkLoginIframe: false,
  silentCheckSsoEnabled: false,
  redirectUri: `${window.location.origin}/`,
};

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <ReactKeycloakProvider
      authClient={keycloak}
      initOptions={initOptions}
      onTokens={onTokens}
      // No bloquea el render completo de la app si Keycloak tarda o no responde.
      isLoadingCheck={() => false}
    >
      <BrowserRouter>
        <AuthProvider>
          <App />
        </AuthProvider>
      </BrowserRouter>
    </ReactKeycloakProvider>
  </React.StrictMode>
);
