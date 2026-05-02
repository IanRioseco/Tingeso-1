import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import bookingService from '../../Services/bookingService';
import paymentService from '../../Services/paymentService';
import { formatPesos } from '../../Utils/currency';
import { formatDateTimeCL } from '../../Utils/dateFormat';
import './payment.css';

const EMPTY_FORM = {
  cardNumber: '',
  cardExpiry: '',
  cvv: '',
};

export default function PaymentPage() {
  const { bookingId } = useParams();
  const navigate = useNavigate();
  const [booking, setBooking] = useState(null);
  const [summary, setSummary] = useState(null);
  const [payment, setPayment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [form, setForm] = useState(EMPTY_FORM);

  useEffect(() => {
    let active = true;

    const loadPaymentData = async () => {
      try {
        setLoading(true);
        setError('');
        const [bookingResponse, summaryResponse] = await Promise.all([
          bookingService.getById(bookingId),
          paymentService.preview(bookingId),
        ]);

        if (!active) {
          return;
        }

        setBooking(bookingResponse.data);
        setSummary(summaryResponse.data || null);
        setPayment(await paymentService.getByBooking(bookingId).then((response) => response.data).catch(() => null));
      } catch (err) {
        if (active) {
          setError('No se pudo cargar la informacion del pago.');
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    loadPaymentData();
    return () => {
      active = false;
    };
  }, [bookingId]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      setProcessing(true);
      setError('');
      setSuccess('');
      const response = await paymentService.pay(bookingId, form);
      setPayment(response.data);
      setSuccess('Pago realizado correctamente.');
      setTimeout(() => {
        navigate('/my-bookings');
      }, 1800);
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo procesar el pago.');
    } finally {
      setProcessing(false);
    }
  };

  if (loading) {
    return <div className="payment-page container">Cargando pago...</div>;
  }

  if (!booking) {
    return (
      <div className="payment-page container">
        <p className="payment-error">No se encontro la reserva.</p>
        <button className="btn btn-secondary" onClick={() => navigate('/my-bookings')}>
          Volver a mis reservas
        </button>
      </div>
    );
  }

  const totalAmount = summary?.totalAmount ?? booking.finalAmount ?? 0;
  const discountAmount = summary?.discountAmount ?? booking.discountAmount ?? 0;
  const discountDetail = summary?.discountDetail || booking.discountDetail || 'Sin detalle de descuento';

  return (
    <div className="payment-page container">
      <div className="payment-header">
        <div>
          <p className="payment-kicker">Pago simulado</p>
          <h1>Reserva #{bookingId}</h1>
          <p className="payment-subtitle">Completa los datos de la tarjeta de prueba para confirmar la reserva.</p>
        </div>
        <button className="btn btn-secondary" onClick={() => navigate('/my-bookings')}>
          Volver
        </button>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="payment-grid">
        <section className="payment-card">
          <h3>Resumen de la reserva</h3>
          <div className="payment-summary-list">
            <div><span>Paquete</span><strong>{booking.packageName || summary?.packageName || '-'}</strong></div>
            <div><span>Pasajeros</span><strong>{booking.passengers ?? summary?.passengers ?? 0}</strong></div>
            <div><span>Fecha de creación</span><strong>{formatDateTimeCL(booking.createdAt)}</strong></div>
            <div><span>Total original</span><strong>{formatPesos(booking.baseAmount ?? summary?.baseAmount ?? 0)}</strong></div>
            <div><span>Descuento</span><strong>-{formatPesos(discountAmount)}</strong></div>
            <div className="summary-total"><span>Total a pagar</span><strong>{formatPesos(totalAmount)}</strong></div>
          </div>
          <p className="payment-detail">{discountDetail}</p>
          {payment && (
            <div className="payment-confirmation">
              <h4>Pago registrado</h4>
              <div><span>Estado</span><strong>{payment.status}</strong></div>
              <div><span>Referencia</span><strong>{payment.transactionRef}</strong></div>
              <div><span>Fecha</span><strong>{formatDateTimeCL(payment.paidAt)}</strong></div>
            </div>
          )}
        </section>

        <section className="payment-card">
          <h3>Datos de la tarjeta simulada</h3>
          <form className="payment-form" onSubmit={handleSubmit}>
            <label>
              Número de tarjeta
              <input
                type="text"
                name="cardNumber"
                value={form.cardNumber}
                onChange={handleChange}
                placeholder="1234 5678 9012 3456"
                required
              />
            </label>
            <div className="payment-row">
              <label>
                Vencimiento
                <input
                  type="text"
                  name="cardExpiry"
                  value={form.cardExpiry}
                  onChange={handleChange}
                  placeholder="MM/YY"
                  required
                />
              </label>
              <label>
                CVV
                <input
                  type="password"
                  name="cvv"
                  value={form.cvv}
                  onChange={handleChange}
                  placeholder="123"
                  required
                />
              </label>
            </div>
            <div className="payment-note">
              El sistema asume que el pago siempre se aprueba. No se realiza validacion externa.
            </div>
            <button type="submit" className="btn btn-primary" disabled={processing}>
              {processing ? 'Procesando pago...' : `Pagar ${formatPesos(totalAmount)}`}
            </button>
          </form>
        </section>
      </div>
    </div>
  );
}
