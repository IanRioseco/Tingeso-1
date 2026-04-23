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
          <li><Link to="/packages" onClick={closeSidebar}>Packages</Link></li>

          {authenticated && (
            <li><Link to="/profile" onClick={closeSidebar}>My Profile</Link></li>
          )}

          {authenticated && !isAdmin() && (
            <li><Link to="/my-bookings" onClick={closeSidebar}>My Bookings</Link></li>
          )}

          {isAdmin() && (
            <>
              <li className="sidebar-section">Admin</li>
              <li><Link to="/admin" onClick={closeSidebar}>Dashboard</Link></li>
              <li><Link to="/admin/packages" onClick={closeSidebar}>Packages</Link></li>
              <li><Link to="/admin/users" onClick={closeSidebar}>Users</Link></li>
              <li><Link to="/admin/reports" onClick={closeSidebar}>Reports</Link></li>
            </>
          )}
        </ul>

        
        <div className="sidebar-auth">
          {authenticated ? (
            <div className="sidebar-user">
              <span>Hi, {userName.split(' ')[0]}</span>
              <button className="btn btn-secondary" onClick={handleLogout}>Logout</button>
            </div>
          ) : (
            <div className="sidebar-auth-buttons">
              <Link to="/login" className="btn btn-secondary" onClick={closeSidebar}>Login</Link>
              <Link to="/register" className="btn btn-primary" onClick={closeSidebar}>Register</Link>
            </div>
          )}
        </div>
      </nav>
    </>
  );
}

export default Navbar;