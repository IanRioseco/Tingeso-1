import { createContext, useContext, useMemo, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // Estado local de usuario para compatibilidad con componentes heredados.
  // La autenticación principal ahora la resuelve Keycloak.
  const [user, setUser] = useState(null);

  const value = useMemo(
    () => ({
      user,
      setUser,
      logout: () => setUser(null),
      isAdmin: () => user?.role === 'ADMIN',
      isLoggedIn: () => Boolean(user),
    }),
    [user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }
  return context;
}
