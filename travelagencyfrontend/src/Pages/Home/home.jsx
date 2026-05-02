// src/pages/Home/Home.jsx
import { useNavigate } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faEarthAmerica,
  faCreditCard,
  faCalendarAlt,
} from '@fortawesome/free-solid-svg-icons';
import './home.css';

export default function Home() {
  const navigate = useNavigate();

  return (
    <div className="home">
      {/* contenedor para el hero de la aplicación */}
      <section className="home-hero">
        <div className="home-hero-content">
          <h1>Viaja sin Fronteras</h1>
          <p>Explora nuestros paquetes de viaje curados a destinos alrededor del mundo.</p>
          <button className="btn btn-primary btn-lg" onClick={() => navigate('/packages')}>
            Explorar Paquetes
          </button>
        </div>
      </section>

      {/* contenedor para las funcionalidades principales de la aplicación */}
      <section className="home-features container">
        {/* div para la funcionalidad de destinos nacionales e internacionales */}
        <div className="home-feature-item">
          <span><FontAwesomeIcon icon={faEarthAmerica} className='home-feature-icon'/></span>
          <h3>Nacional e Internacional</h3>
          <p>Paquetes a destinos de Chile y el mundo.</p>
        </div>
        {/* div para la funcionalidad de pagos seguros */}
        <div className="home-feature-item">
          <span><FontAwesomeIcon icon={faCreditCard} className='home-feature-icon'/></span>
          <h3>Pagos Seguros</h3>
          <p>Proceso de pago en línea seguro y sencillo.</p>
        </div>
        {/* div para la funcionalidad de reserva de paquetes */}
        <div className="home-feature-item">
          <span><FontAwesomeIcon icon={faCalendarAlt} className='home-feature-icon'/></span>
          <h3>Reserva Fácil</h3>
          <p>Reserva tu viaje en minutos desde cualquier lugar.</p>
        </div>
      </section>
    </div>
  );
}