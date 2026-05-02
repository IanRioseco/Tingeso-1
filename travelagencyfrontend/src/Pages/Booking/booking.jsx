import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import packageService from '../../Services/packageService';
import bookingService from '../../Services/bookingService';
import userService from '../../Services/UserService';
import promotionService from '../../Services/promotionService';
import { formatPesos } from '../../Utils/currency';
import { getAvailableSlots } from '../../Utils/packageFilters';
import { getFriendlyBookingError } from '../../Utils/bookingErrors';
import { getOrCreateSessionId, resetBookingSessionForUser } from '../../Utils/bookingSession';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faCalendarCheck,
  faCircleInfo,
  faClock,
  faMoneyBillWave,
  faPersonWalkingArrowRight,
  faReceipt,
  faUserGroup,
} from '@fortawesome/free-solid-svg-icons';
import './booking.css';


export default function BookingPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { keycloak } = useKeycloak();
  const [pkg, setPkg] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);
  const [passengers, setPassengers] = useState(1);
  const [sessionId, setSessionId] = useState('');
  const [preview, setPreview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Carga los datos de la reserva.
  useEffect(() => {
    let active = true;
    // Carga los datos del paquete, el usuario actual y la sesión de reserva en paralelo.
    const loadData = async () => {
      try {
        setLoading(true);
        setError('');
        // Carga los datos del paquete, el usuario actual y la sesión de reserva.
        const [packageResponse, userResponse] = await Promise.all([
          packageService.getById(id),
          userService.me(),
        ]);

        if (!active) {
          return;
        }
        // Inicializa los datos del paquete, el usuario actual y la sesión de reserva.
        setPkg(packageResponse.data);
        setCurrentUser(userResponse.data);
        setSessionId(getOrCreateSessionId(userResponse.data.id));
      } catch (err) {
        if (active) {
          setError('No se pudo cargar la información necesaria para la reserva.');
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    loadData();
    // Elimina el estado de carga de la vista.
    return () => {
      active = false;
    };
  }, [id]);
  // Calcula el precio base de la reserva.
  const baseAmount = useMemo(() => {
    if (!pkg) {
      return 0;
    }
    return Number(pkg.price || 0) * Number(passengers || 1);
  }, [pkg, passengers]);

  useEffect(() => {
    let active = true;
    // Calcula el descuento de la reserva.
    const calculatePreview = async () => {
      if (!pkg || !currentUser) {
        return;
      }

      try {
        setPreviewLoading(true);
        // Calcula el descuento de la reserva.
        const response = await promotionService.calculate({
          baseAmount,
          passengers: Number(passengers),
          sessionId,
        });

        if (active) {
          setPreview(response.data);
        }
      } catch (err) {
        if (active) {
          setPreview(null);
        }
      } finally {
        if (active) {
          setPreviewLoading(false);
        }
      }
    };
    // Calcula el descuento de la reserva.
    calculatePreview();
    // Elimina el estado de carga de la vista.
    return () => {
      active = false;
    };
  }, [baseAmount, currentUser, passengers, pkg, sessionId]);
  // Cambia el número de pasajeros en la reserva.
  const handlePassengersChange = (event) => {
    const value = Math.max(1, Number(event.target.value || 1));
    setPassengers(value);
  };
  // Extrae el mensaje de error de la respuesta de la API.
  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!keycloak.authenticated) {
      navigate('/login');
      return;
    }
    // Si faltan datos para crear la reserva, muestra un mensaje de error.
    if (!pkg || !currentUser) {
      setError('Faltan datos para crear la reserva.');
      return;
    }
    // Si la cantidad de pasajeros excede los cupos disponibles, muestra un mensaje de error.
    if (Number(passengers) > getAvailableSlots(pkg)) {
      setError('La cantidad de pasajeros excede los cupos disponibles.');
      return;
    }

    try {
      setSaving(true);
      setError('');
      // Crea la reserva de la reserva.
      const response = await bookingService.create({
        packageId: pkg.id,
        passengers: Number(passengers),
        sessionId,
      });
      // Elimina el identificador de sesión de la reserva.
      setSessionId(resetBookingSessionForUser(currentUser.id));
      setSuccess(`Reserva creada correctamente. ID: ${response.data.id}. Redirigiendo al pago...`);
      setTimeout(() => {
        navigate(`/payment/${response.data.id}`);
      }, 2000);
    } catch (err) {
      setError(getFriendlyBookingError(err, passengers));
    } finally {
      setSaving(false);
    }
  };
  // Si se está cargando la información de la reserva, mostramos un mensaje de carga
  if (loading) {
    return <div className="booking-page container">Cargando proceso de reserva...</div>;
  }
  // Si ocurre algún error, mostramos un mensaje de error
  if (error && !pkg) {
    return (
      <div className="booking-page container">
        <p className="booking-error">{error}</p>
        <button className="btn btn-secondary" onClick={() => navigate('/packages')}>
          Volver a paquetes
        </button>
      </div>
    );
  }

  if (!pkg || !currentUser) {
    return null;
  }
  // Devuelve el detalle de la reserva.
  const previewData = preview || {
    discountAmount: 0,
    finalAmount: baseAmount,
    discountDetail: 'Sin simulación de descuentos disponible',
  };

  return (
    <div className="booking-page container">
      {/* Encabezado de la reserva */}
      <div className="booking-header">
        <div>
          <p className="booking-kicker">Proceso de reserva</p>
          <h1>{pkg.name}</h1>
          <p className="booking-subtitle">Completa los datos y revisa el total antes de confirmar.</p>
        </div>
        <button className="btn btn-secondary" onClick={() => navigate(`/packages/${pkg.id}`)}>
          Volver al detalle
        </button>
      </div>
        {/* Tarjeta de resumen */}
      <div className="booking-grid">
        <section className="booking-card">
          <h3>
            <FontAwesomeIcon icon={faCircleInfo} className="booking-card-icon" />
            Resumen del paquete
          </h3>
          {/* Lista de detalles del paquete */} 
          <div className="booking-summary-list">
            <div><strong>Destino:</strong> {pkg.destination}</div>
            <div><strong>Fechas:</strong> {pkg.startDate} - {pkg.endDate}</div>
            <div><strong>Cupos disponibles:</strong> {getAvailableSlots(pkg)}</div>
            <div><strong>Precio base:</strong> {formatPesos(pkg.price)}</div>
            <div><strong>Tipo:</strong> {pkg.travelType || 'No definido'}</div>
            <div><strong>Temporada:</strong> {pkg.season || 'No definida'}</div>
          </div>
        </section>
        {/* Tarjeta de detalles */}
        <section className="booking-card">
          <h3>
            <FontAwesomeIcon icon={faUserGroup} className="booking-card-icon" />
            Datos de la reserva
          </h3>
          {/* Formulario para crear la reserva */}
          <form onSubmit={handleSubmit} className="booking-form">
            {/* Campo para la cantidad de pasajeros */}
            <label>
              Cantidad de pasajeros
              <input
                type="number"
                min="1"
                max={getAvailableSlots(pkg)}
                value={passengers}
                onChange={handlePassengersChange}
                required
              />
            </label>
            {/* Campo para la sesión de compra */}
            <label>
              Sesión de compra
              <input type="text" value={sessionId} readOnly />
            </label>
            {/* Tarjeta de resumen del descuento */}
            <div className="booking-preview">
              <h4>
                <FontAwesomeIcon icon={faReceipt} className="booking-card-icon" />
                Resumen de precios
              </h4>
              {/* Detalles del precio base y del descuento */}
              <div className="booking-preview-row"><span>Precio original</span><strong>{formatPesos(baseAmount)}</strong></div>
              <div className="booking-preview-row"><span>Descuento</span><strong>-{formatPesos(previewData.discountAmount)}</strong></div>
              <div className="booking-preview-row total"><span>Total final</span><strong>{formatPesos(previewData.finalAmount)}</strong></div>
              {/* Detalles del descuento */}
              <p className="booking-discount-detail">
                <FontAwesomeIcon icon={faClock} className="booking-card-icon" />
                {previewLoading ? 'Calculando descuentos...' : previewData.discountDetail}
              </p>
            </div>
            {/* Botón de confirmación de la reserva */}
            {error && <p className="booking-error">{error}</p>}
            {success && <p className="booking-success">{success}</p>}
            {/* Botón de confirmación de la reserva */}
            <button type="submit" className="btn btn-primary" disabled={saving}>
              <FontAwesomeIcon icon={faCalendarCheck} /> {saving ? 'Creando reserva...' : 'Confirmar reserva'}
            </button>
          </form>
        </section>
        </div>
      {/* Tarjeta de reserva */}
      <section className="booking-card booking-notes">
        <h3>
          <FontAwesomeIcon icon={faPersonWalkingArrowRight} className="booking-card-icon" />
          Consideraciones
        </h3>
        <ul>
          <li>La reserva se crea inicialmente en estado pendiente de pago.</li>
          <li>Los cupos se descuentan al crear la reserva y se liberan si se cancela o expira.</li>
          <li>Si el pago no se completa dentro del plazo, la reserva expira y los cupos se liberan.</li>
          <li>Después de crear la reserva, podrás completar el pago simulado en la siguiente pantalla.</li>
        </ul>
      </section>
    </div>
  );
}