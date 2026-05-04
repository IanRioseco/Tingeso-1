// src/components/PackageCard/packageCard.jsx
import { useNavigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import { formatPesos } from '../../Utils/currency';
import { getAvailableSlots } from '../../Utils/packageFilters';
import './packageCard.css';

export default function PackageCard({ pkg }) {
  const navigate = useNavigate();
  const { keycloak } = useKeycloak();

  const handleBook = () => {
    if (keycloak.authenticated) {
      navigate(`/booking/${pkg.id}`);
      return;
    }

    navigate('/login');
  };

  return (
    {/* Tarjeta de paquete */},
    <div className="package-card">
      <div className="package-card-header">
        <h3 className="package-card-name">{pkg.name}</h3>
        <span className={`badge badge-${pkg.status?.toLowerCase().replace('_', '-')}`}>
          {pkg.status}
        </span>
      </div>
      {/* Contenedor de detalles de la tarjeta */}
      <p className="package-card-destination">📍 {pkg.destination}</p>
      <p className="package-card-description">{pkg.description?.slice(0, 100)}...</p>

      {/* Contenedor de detalles de la tarjeta */}
      <div className="package-card-details">
        <span>📅 {pkg.startDate} → {pkg.endDate}</span>
        <span>⏱ {pkg.durationDays} días</span>
        <span>👥 {getAvailableSlots(pkg)} cupos disponibles</span>
      </div>

      {/* Contenedor de acciones de la tarjeta */}
      <div className="package-card-footer">
        <span className="package-card-price">{formatPesos(pkg.price)}</span>
        <div className="package-card-actions">
          <button
            className="btn btn-secondary"
            onClick={() => navigate(`/packages/${pkg.id}`)}
          >
            Ver Detalles
          </button>
          <button
            className="btn btn-primary"
            onClick={handleBook}
          >
            Reservar
          </button>
        </div>
      </div>
    </div>
  );
}