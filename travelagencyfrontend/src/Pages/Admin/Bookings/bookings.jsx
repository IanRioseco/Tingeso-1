import { useEffect, useState } from 'react';
import bookingService from '../../../Services/bookingService';
import { formatDateTimeCL } from '../../../Utils/dateFormat';
import { formatPesos } from '../../../Utils/currency';
import './bookings.css';

export default function AdminBookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadBookings = async () => {
    try {
      setLoading(true);
      setError('');
      const response = await bookingService.getAll();
      setBookings(response.data || []);
    } catch (err) {
      setError('No se pudieron cargar las reservas.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBookings();
  }, []);

  const handleCopyReceipt = async (bookingId) => {
    const receiptUrl = `${window.location.origin}/api/bookings/${bookingId}/receipt`;
    try {
      await navigator.clipboard.writeText(receiptUrl);
    } catch {
      window.prompt('Copia este enlace del comprobante', receiptUrl);
    }
  };

  if (loading) {
    return <div className="admin-bookings container">Cargando reservas...</div>;
  }

  return (
    <div className="admin-bookings container">
      <div className="admin-bookings-header">
        <div>
          <p className="admin-bookings-kicker">Administracion</p>
          <h1>Reservas del sistema</h1>
        </div>
        <button className="btn btn-secondary" onClick={loadBookings}>
          Recargar
        </button>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="admin-bookings-table-wrap">
        <table className="admin-bookings-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Cliente</th>
              <th>Paquete</th>
              <th>Pasajeros</th>
              <th>Total</th>
              <th>Estado</th>
              <th>Creada</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {bookings.length === 0 ? (
              <tr>
                <td colSpan="8" className="empty-row">No hay reservas registradas</td>
              </tr>
            ) : bookings.map((booking) => (
              <tr key={booking.id}>
                <td>{booking.id}</td>
                <td>{booking.userName || '-'}</td>
                <td>{booking.packageName || '-'}</td>
                <td>{booking.passengers}</td>
                <td>{formatPesos(booking.finalAmount)}</td>
                <td>
                  <span className={`booking-status ${booking.bookingStatus?.toLowerCase() || 'pending'}`}>
                    {booking.bookingStatus}
                  </span>
                </td>
                <td>{formatDateTimeCL(booking.createdAt)}</td>
                <td className="actions-cell">
                  <button className="btn btn-outline-secondary btn-sm" onClick={() => handleCopyReceipt(booking.id)}>
                    Comprobante
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
