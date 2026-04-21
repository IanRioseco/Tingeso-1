import { Link, Navigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import './register.css';

export default function Register() {
  const { keycloak, initialized } = useKeycloak();

  if (keycloak.authenticated) {
    return <Navigate to="/" replace />;
  }

  const handleRegister = () => {
    keycloak.register({
      redirectUri: `${window.location.origin}/login`,
    });
  };

  return (
    <div className="register-page">
      <div className="register-card">
        <h2>Registro de usuarios</h2>
        <p className="register-subtitle">
          El alta de cuenta, validación de credenciales e intentos fallidos se gestionan en Keycloak.
        </p>

        {!initialized && (
          <p className="register-hint">Conectando con el servidor de autenticación...</p>
        )}

        <button type="button" className="btn btn-primary register-btn" onClick={handleRegister}>
          Registrarme en Keycloak
        </button>

        <p className="register-login">
          ¿Ya tienes cuenta? <Link to="/login">Inicia sesión aquí</Link>
        </p>
      </div>
    </div>
  );
}
