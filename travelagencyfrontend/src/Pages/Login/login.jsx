import { Link, Navigate, useLocation } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import './login.css';

export default function Login() {
  const { keycloak, initialized } = useKeycloak();
  const location = useLocation();

  const from = location.state?.from?.pathname || '/';

  if (keycloak.authenticated) {
    return <Navigate to={from} replace />;
  }

  const handleLogin = () => {
    keycloak.login({
      redirectUri: `${window.location.origin}${from}`,
    });
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h2>Bienvenido!</h2>
        <p className="login-subtitle">Inicia sesión con tu cuenta de Travel Agency</p>

        {!initialized && (
          <p className="login-subtitle">Conectando con el servidor de autenticacion...</p>
        )}

        <button className="btn btn-primary login-btn" onClick={handleLogin}>
          Ingresar con Keycloak
        </button>

        <p className="login-register">
          No tienes cuenta? <Link to="/register">Registrate aqui</Link>
        </p>
      </div>
    </div>
  );
}