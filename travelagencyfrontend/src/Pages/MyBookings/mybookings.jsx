import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import bookingService from '../../Services/bookingService';
import { getFriendlyBookingError } from '../../Utils/bookingErrors';
import BookingCard from '../../Components/BookingCard/bookingCard';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faClock,
  faClipboardCheck,
  faClipboardList,
  faDollarSign,
  faExclamationTriangle,
  faLocationDot,
  faPersonWalkingArrowRight,
  faReceipt,
  faUserGroup,
}from '@fortawesome/free-solid-svg-icons';
import './mybookings.css';

export default function MyBookings() {
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();
    const [expanded, setExpanded] = useState(false);
    const [payments, setPayments] = useState([]);
    const { keycloak } = useKeycloak();

    const loadBookings = async (active = true) => {
        try {
            setLoading(true);
            setError('');
            const response = await bookingService.getMyBookings();
            if (active) {
                setBookings(response.data);
            }
        } catch (err) {
            if (active) {
                setError('No se pudo cargar tus reservas. Intenta nuevamente.');
            }
        } finally {
            if (active) {
                setLoading(false);
            }
        }
    };

    useEffect(() => {
        let active = true;
        loadBookings(active);
        return () => {
            active = false;
        };
    }, []);

    const handleExpand = (id) => {
        setExpanded(id);
    };

    const handleCancel = async (id) => {
        try {
            setError('');
            setSuccess('');
            await bookingService.cancel(id);
            loadBookings();
        } catch (err) {
            setError('Error al cancelar la reserva. Por favor, revisa los datos e intenta nuevamente.');
        }
    };

    const handlePay = (bookingId) => {
        navigate(`/payment/${bookingId}`);
    };

    if (loading) {
        return <div className="container" style={{ padding: '4rem' }}>Cargando tus reservas...</div>;
    }

    if (error) {
        {/* Si ocurre algún error, mostramos un mensaje de error 
            y un botón para volver a la página de paquetes */}
        return (
            <div className="container" style={{ padding: '4rem' }}>
                <p>{error}</p>
                <button className="btn btn-secondary" onClick={() => navigate('/packages')}>
                    Volver a paquetes
                </button>
            </div>
        );
    }

    return (
        <div className="my-bookings container">
            <div className="my-bookings-header">
                <h1>Mis Reservas</h1>
                <button className="btn btn-primary" onClick={() => navigate('/packages')}>
                    Reservar Nueva
                </button>
            </div>
            {success && <div className="alert alert-success">{success}</div>}
            {error && <div className="alert alert-danger">{error}</div>}

            {/* Lista de reservas de usuario */}
            <div className="my-bookings-list">
                {bookings.map((booking) => (
                    <BookingCard
                        key={booking.id}
                        booking={booking}
                        onExpand={handleExpand}
                        onCancel={handleCancel}
                        onPay={handlePay}
                    />
                ))}
            </div>
        </div>
        );
    }    