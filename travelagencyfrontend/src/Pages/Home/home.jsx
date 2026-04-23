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
          <h1>Discover Your Next Adventure</h1>
          <p>Explore our curated travel packages to destinations around the world.</p>
          <button className="btn btn-primary btn-lg" onClick={() => navigate('/packages')}>
            Browse Packages
          </button>
        </div>
      </section>

      {/* contenedor para las funcionalidades principales de la aplicación */}
      <section className="home-features container">
        {/* div para la funcionalidad de destinos nacionales e internacionales */}
        <div className="home-feature-item">
          <span><FontAwesomeIcon icon={faEarthAmerica} className='home-feature-icon'/></span>
          <h3>National & International</h3>
          <p>Packages to destinations across Chile and the world.</p>
        </div>
        {/* div para la funcionalidad de pagos seguros */}
        <div className="home-feature-item">
          <span><FontAwesomeIcon icon={faCreditCard} className='home-feature-icon'/></span>
          <h3>Secure Payments</h3>
          <p>Safe and simple online payment process.</p>
        </div>
        {/* div para la funcionalidad de reserva de paquetes */}
        <div className="home-feature-item">
          <span><FontAwesomeIcon icon={faCalendarAlt} className='home-feature-icon'/></span>
          <h3>Easy Booking</h3>
          <p>Reserve your trip in minutes from anywhere.</p>
        </div>
      </section>
    </div>
  );
}