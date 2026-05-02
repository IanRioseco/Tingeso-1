const ACCESS_TOKEN_KEY = 'kc_token';
const REFRESH_TOKEN_KEY = 'kc_refresh_token';
const ID_TOKEN_KEY = 'kc_id_token';

// Almacena los tokens de Keycloak en el navegador local.
export const saveKeycloakTokens = (tokens = {}) => {
  const { token, refreshToken, idToken } = tokens;
  // Almacena los tokens de Keycloak en el navegador local.
  localStorage.setItem(ACCESS_TOKEN_KEY, token || '');
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken || '');
  localStorage.setItem(ID_TOKEN_KEY, idToken || '');
};
// Obtiene el token de acceso almacenado.
export const getAccessToken = () => localStorage.getItem(ACCESS_TOKEN_KEY);
// Obtiene el token de actualización almacenado.
export const clearKeycloakTokens = () => {
  // Elimina los tokens de Keycloak almacenados.
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(ID_TOKEN_KEY);
};
