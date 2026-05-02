// Utilidades para gestionar la autenticación y autorización del usuario.
export const isAdminUser = (keycloak) => Boolean(keycloak?.hasRealmRole?.('ADMIN'));

// Obtiene el nombre completo del usuario autenticado.
export const getDisplayName = (keycloak) => {
  return keycloak?.tokenParsed?.name || keycloak?.tokenParsed?.preferred_username || 'User';
};
