import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { ReactKeycloakProvider } from '@react-keycloak/web';
import './index.css';
import App from './App.jsx';
import { AuthProvider } from './context/AuthContext.jsx';
import keycloak from './Services/keycloak.js';

console.log('[DEBUG] main.jsx loaded');
console.log('[DEBUG] keycloak instance:', keycloak);
console.log('[DEBUG] keycloak.init:', typeof keycloak.init);

const onTokens = (tokens = {}) => {
  const { token, refreshToken, idToken } = tokens;
  console.log('[DEBUG] onTokens called with:', { token: !!token, refreshToken: !!refreshToken, idToken: !!idToken });
  localStorage.setItem('kc_token', token || '');
  localStorage.setItem('kc_refresh_token', refreshToken || '');
  localStorage.setItem('kc_id_token', idToken || '');
};

const initOptions = {
  // Diagnóstico: Deshabilitado PKCE y usando HTTP
  pkceMethod: false,
  checkLoginIframe: false,
  onLoad: 'check-sso',
  silentCheckSsoRedirectUri: 'http://3.12.42.217/silent-check-sso.html',
  redirectUri: 'http://3.12.42.217/',
};

const root = ReactDOM.createRoot(document.getElementById('root'));
console.log('[DEBUG] about to render React app with ReactKeycloakProvider');
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
console.log('[DEBUG] React app rendered');