import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import packageService from '../../Services/packageService';
import './packagedetail.css';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
    faLocationDot,
    faMoneyBillWave,
    faClock,
    faCalendar,
    faPlane,
    faUserFriends,
    faUser,
    faCalendarDays,

 } from '@fortawesome/free-solid-svg-icons';


export default function PackageDetail() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { isLoggedIn } = useAuth();
    const [pkg, setPkg] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // Carga el detalle del paquete al montar el componente
    //  o cuando cambia el ID
    useEffect(() => {
        let active = true;

        // Carga el detalle del paquete desde el backend
        const loadPackage = async () => {
            try {
                setLoading(true);
                setError('');
                const res = await packageService.getById(id);
                if (active) {
                    setPkg(res.data);
                }
            } catch (err) {
                if (active) {
                    setError('No se pudo cargar el detalle del paquete.');
                }
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        };

        loadPackage();
        return () => {
            active = false;
        };
    }, [id]);

    /* Función para bookear el paquete */
    const handleBook = () => {
        if (isLoggedIn()) {
            navigate(`/booking/${pkg.id}`);
            return;
        }
        navigate('/login');
    };

    if (loading) {
        return <div className="container" style={{ padding: '4rem' }}>Cargando paquete...</div>;
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

    if (!pkg) {
        return null;
    }

    return (
        {/* Detalle del paquete turístico */},
        <div className="package-detail container">
            <button className="btn btn-secondary" onClick={() => navigate('/packages')}>
                Volver
            </button>

            {/* Tarjeta del paquete */}
            <div className="package-detail-card">

                {/* Cabecera del detalle del paquete */}
                <div className="package-detail-header">
                    <div >
                        <h1>{pkg.name}</h1>
                        <p className="package-detail-destination">
                            <FontAwesomeIcon icon={faLocationDot} className='package-detail-icon'/>
                            <strong>Destino:</strong> {pkg.destination}
                        </p>
                        <span className="package-detail-status">
                            <strong>Estado:</strong> {pkg.status === 'AVAILABLE' ? <span>Disponible</span> : <span>No disponible</span>}
                        </span>
                    </div>

                    {/* contenedor para el detalle del paquete */}
                    <div className='package-detail-grid'> 

                        {/* Información del paquete */}
                        <div className='package-detail-grid-info'>
                            <h3>Información sobre el paquete</h3>
                            <ul>{pkg.description.split(',').map((service, index) => (
                                    <li key={index}>{service.trim()}</li>
                                    ))}
                            </ul>
                            
                            <p><strong>Condiciones del paquete</strong>
                                <ul>
                                    {pkg.conditions
                                    .split('.')
                                    .map((condition) => condition.trim())
                                    .filter((condition) => condition.length > 0)
                                    .map((condition, index) => (
                                        <li key={index}>{condition}</li>
                                    ))}
                                </ul>
                            </p>
                            {/* contenedor para el detalle de los metadatos del paquete */}
                            <div className='package-detail-grid-info-meta'>

                                {/* div para el precio del paquete */}
                                <div>
                                    <FontAwesomeIcon icon={faMoneyBillWave} className='package-detail-icon'/>
                                    <strong>Precio:</strong> ${pkg.price} 
                                </div>
                                {/* div para la duración del paquete */}
                                <div>
                                    <FontAwesomeIcon icon={faClock} className='package-detail-icon'/>
                                    <strong>Duración:</strong> {pkg.durationDays} días
                                </div>
                                {/* div para las fechas del paquete */}
                                <div>
                                    <FontAwesomeIcon icon={faCalendar} className='package-detail-icon'/>
                                    <strong>Fechas:</strong> {pkg.startDate} - {pkg.endDate}
                                </div>
                                {/* div para el tipo de viaje del paquete */}
                                <div>
                                    <FontAwesomeIcon icon={faPlane} className='package-detail-icon'/>
                                    <strong>Tipo:</strong> {pkg.travelType}
                                </div>
                                {/* div para la temporada del paquete */}
                                <div>
                                    <FontAwesomeIcon icon={faCalendarDays} className='package-detail-icon'/>
                                    <strong>Temporada:</strong> {pkg.season}
                                </div>
                                {/* div para el número de cupos disponibles del paquete */}
                                <div>
                                    <FontAwesomeIcon icon={faUserFriends} className='package-detail-icon'/>
                                    <strong>Cupos disponibles:</strong> {pkg.availableSlots}
                                </div>
                            </div>
                            
                            {/* contenedor para el detalle de los servicios del paquete */}
                            <div className='package-detail-grid-info-services'>
                                <h3>Servicios incluidos</h3>
                                <ul>
                                    {pkg.servicesIncluded?.split(';').map((service, index) => (
                                        <li key={index}>{service.trim()}</li>
                                    )) || <p>No hay servicios incluidos.</p>}
                                </ul>
                            </div>

                            {/* contenedor para el detalle de las restricciones del paquete */}
                            <div className='package-detail-grid-info-conditions'>
                                <h3>Restricciones del paquete</h3>
                                <ul>
                                    {pkg.restrictions
                                    .split('.')
                                    .map((condition) => condition.trim())
                                    .filter((condition) => condition.length > 0)
                                    .map((condition, index) => (
                                        <li key={index}>{condition}</li>
                                    ))}
                                </ul>
                            </div>

                        </div>
                    </div>
                </div>

                {/* Botón para reservar el paquete */}
                <button className="btn btn-primary" onClick={handleBook}>
                    Reservar este paquete
                </button>
            </div>
        </div>
    );
}

