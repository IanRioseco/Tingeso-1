import { Navigate, useLocation } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';

function PrivateRoute({ children, roles = [] }) {
  const { keycloak, initialized } = useKeycloak();
  const location = useLocation();

  if (!initialized) {
    return <div>Cargando autenticacion...</div>;
  }

  if (!keycloak.authenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (roles.length > 0) {
    const hasRole = roles.some((role) => keycloak.hasRealmRole(role));
    if (!hasRole) {
      return <Navigate to="/" replace />;
    }
  }

  return children;
}

export default PrivateRoute;