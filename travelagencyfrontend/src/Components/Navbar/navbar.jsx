// src/components/Navbar/Navbar.jsx
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import './navbar.css';

export function Navbar({ onSidebarStateChange }) {
  const { keycloak, initialized } = useKeycloak();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const authenticated = initialized && keycloak.authenticated;
  const isAdmin = () => keycloak.hasRealmRole('ADMIN');
  const userName =
    keycloak.tokenParsed?.name ||
    keycloak.tokenParsed?.preferred_username ||
    'User';

  const handleLogout = () => {
    keycloak.logout({ redirectUri: window.location.origin });
  };

  const closeSidebar = () => {
    setSidebarOpen(false);
  };

  useEffect(() => {
    if (onSidebarStateChange) {
      onSidebarStateChange(sidebarOpen);
    }
  }, [sidebarOpen, onSidebarStateChange]);

  return (
    <>
      {/* Botón para abrir/cerrar el sidebar */}
      <button
        className={`sidebar-toggle ${sidebarOpen ? 'open' : ''}`}
        type="button"
        onClick={() => setSidebarOpen((prev) => !prev)}
        aria-label="Abrir menu"
      >
        {sidebarOpen ? 'x' : '≡'}
      </button>

      {/* Overlay para cerrar el sidebar al hacer click fuera de él */}
      <div className={`sidebar-overlay ${sidebarOpen ? 'open' : ''}`} onClick={closeSidebar} />
      {/* Sidebar principal */}
      <nav className={`sidebar ${sidebarOpen ? 'open' : ''}`}>
        <div className="sidebar-header">
          <Link to="/" className="sidebar-brand" onClick={closeSidebar}>TravelAgency</Link>
        </div>

        {/* Enlaces de navegación */}
        <ul className="sidebar-links">
          <li><Link to="/packages" onClick={closeSidebar}>Paquetes</Link></li>

          {authenticated && (
            <li><Link to="/profile" onClick={closeSidebar}>Mi Perfil</Link></li>
          )}

          {authenticated && !isAdmin() && (
            <li><Link to="/my-bookings" onClick={closeSidebar}>Mis Reservas</Link></li>
          )}

          {isAdmin() && (
            <>
              <li className="sidebar-section">Administrador</li>
              <li><Link to="/admin" onClick={closeSidebar}>Panel de Control</Link></li>
              <li><Link to="/Admin/ManagePackage" onClick={closeSidebar}>Paquetes</Link></li>
              <li><Link to="/admin/users" onClick={closeSidebar}>Usuarios</Link></li>
              <li><Link to="/Admin/ManagePromotions" onClick={closeSidebar}>Promociones</Link></li>
              <li><Link to="/admin/reports" onClick={closeSidebar}>Reportes</Link></li>
            </>
          )}
        </ul>

        {/* Sección de autenticación en el sidebar */}
        <div className="sidebar-auth">
          {authenticated ? (
            <div className="sidebar-user">
              <span>Hola, {userName.split(' ')[0]}</span>
              <button className="btn btn-secondary" onClick={handleLogout}>Cerrar sesión</button>
            </div>
          ) : (
            <div className="sidebar-auth-buttons">
              <Link to="/login" className="btn btn-secondary" onClick={closeSidebar}>Iniciar sesión</Link>
              <Link to="/register" className="btn btn-primary" onClick={closeSidebar}>Registrarse</Link>
            </div>
          )}
        </div>
      </nav>
    </>
  );
}

export default Navbar;