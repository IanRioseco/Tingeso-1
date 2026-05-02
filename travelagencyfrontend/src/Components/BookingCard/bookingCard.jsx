import { formatPesos } from '../../Utils/currency';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCalendarAlt, faUsers, faInfoCircle, faBan, faCreditCard } from '@fortawesome/free-solid-svg-icons';
import './bookingCard.css';

export default function BookingCard({ booking, onExpand, onCancel, onPay }) {
  const isConfirmed = booking.bookingStatus === 'CONFIRMED';
  const isPending = booking.bookingStatus === 'PENDING';
  
  const statusLabel = isConfirmed ? 'Confirmada' : isPending ? 'Pendiente' : booking.bookingStatus;
  const statusClass = `badge booking-badge-${booking.bookingStatus.toLowerCase()}`;

  return (
    <div className="booking-card">
      <div className="booking-card-header">
        <h3 className="booking-card-name">{booking.packageName}</h3>
        <span className={statusClass}>
          {statusLabel}
        </span>
      </div>
      
      <div className="booking-card-details">
        <div className="booking-detail-item">
          <FontAwesomeIcon icon={faUsers} className="detail-icon" />
          <span>{booking.passengers} pasajero(s)</span>
        </div>
        <div className="booking-detail-item">
          <FontAwesomeIcon icon={faCalendarAlt} className="detail-icon" />
          <span>Realizada el: {new Date(booking.createdAt).toLocaleDateString()}</span>
        </div>
      </div>

      <div className="booking-card-footer">
        <span className="booking-card-price">{formatPesos(booking.finalAmount)}</span>
        
        <div className="booking-card-actions">
          <button 
            className="btn btn-outline-secondary btn-sm" 
            onClick={() => onExpand(booking.id)}
          >
            <FontAwesomeIcon icon={faInfoCircle} /> Detalles
          </button>
          
          {(isConfirmed || isPending) && (
            <button 
              className="btn btn-danger btn-sm" 
              onClick={() => onCancel(booking.id)}
            >
              <FontAwesomeIcon icon={faBan} /> Cancelar
            </button>
          )}

          {isPending && onPay && (
            <button
              className="btn btn-primary btn-sm"
              onClick={() => onPay(booking.id)}
            >
              <FontAwesomeIcon icon={faCreditCard} /> Pagar
            </button>
          )}
        </div>
      </div>
    </div>
  );
}