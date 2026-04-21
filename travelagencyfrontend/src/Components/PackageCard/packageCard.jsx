// src/components/PackageCard/packageCard.jsx
import { useNavigate } from 'react-router-dom';
import './PackageCard.css';

export default function PackageCard({ pkg }) {
  const navigate = useNavigate();

  return (
    <div className="package-card">
      <div className="package-card-header">
        <h3 className="package-card-name">{pkg.name}</h3>
        <span className={`badge badge-${pkg.status?.toLowerCase().replace('_', '-')}`}>
          {pkg.status}
        </span>
      </div>

      <p className="package-card-destination">📍 {pkg.destination}</p>
      <p className="package-card-description">{pkg.description?.slice(0, 100)}...</p>

      <div className="package-card-details">
        <span>📅 {pkg.startDate} → {pkg.endDate}</span>
        <span>⏱ {pkg.durationDays} days</span>
        <span>👥 {pkg.availableSlots} slots available</span>
      </div>

      <div className="package-card-footer">
        <span className="package-card-price">${pkg.price?.toLocaleString()}</span>
        <button
          className="btn btn-primary"
          onClick={() => navigate(`/packages/${pkg.id}`)}
        >
          View Details
        </button>
      </div>
    </div>
  );
}