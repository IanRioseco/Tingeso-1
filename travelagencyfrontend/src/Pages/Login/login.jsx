import { Link, Navigate, useLocation } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import './login.css';

export default function Login() {
  const { keycloak, initialized } = useKeycloak();
  const location = useLocation();

  const from = location.state?.from?.pathname || '/packages';

  if (keycloak.authenticated) {
    return <Navigate to={from} replace />;
  }

  const handleLogin = () => {
    console.log('[DEBUG] handleLogin called');
    console.log('[DEBUG] keycloak object:', keycloak);
    console.log('[DEBUG] keycloak.login type:', typeof keycloak.login);
    console.log('[DEBUG] about to call keycloak.login with redirectUri:', `${window.location.origin}${from}`);
    try {
      keycloak.login({
        prompt: 'login',
        redirectUri: `${window.location.origin}${from}`,
      });
      console.log('[DEBUG] keycloak.login() called successfully');
    } catch (err) {
      console.error('[DEBUG] Error calling keycloak.login():', err);
    }
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