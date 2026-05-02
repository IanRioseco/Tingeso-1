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
        <h2>Registro de Usuarios</h2>
        <p className="register-subtitle">
          Crea tu cuenta en Travel Agency para acceder a todos nuestros servicios
        </p>

        {!initialized && (
          <p className="register-hint">Conectando con el servidor de autenticación...</p>
        )}

        <button type="button" className="btn btn-primary register-btn" onClick={handleRegister}>
          Registrarse
        </button>

        <p className="register-login">
          ¿Ya tienes cuenta? <Link to="/login">Inicia sesión aquí</Link>
        </p>
      </div>
    </div>
  );
}
