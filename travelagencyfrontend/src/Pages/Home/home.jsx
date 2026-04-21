// src/pages/Home/Home.jsx
import { useNavigate } from 'react-router-dom';
import './home.css';

export default function Home() {
  const navigate = useNavigate();

  return (
    <div className="home">
      <section className="home-hero">
        <div className="home-hero-content">
          <h1>Discover Your Next Adventure</h1>
          <p>Explore our curated travel packages to destinations around the world.</p>
          <button className="btn btn-primary btn-lg" onClick={() => navigate('/packages')}>
            Browse Packages
          </button>
        </div>
      </section>

      <section className="home-features container">
        <div className="home-feature">
          <span className="home-feature-icon">🌍</span>
          <h3>National & International</h3>
          <p>Packages to destinations across Chile and the world.</p>
        </div>
        <div className="home-feature">
          <span className="home-feature-icon">💳</span>
          <h3>Secure Payments</h3>
          <p>Safe and simple online payment process.</p>
        </div>
        <div className="home-feature">
          <span className="home-feature-icon">📋</span>
          <h3>Easy Booking</h3>
          <p>Reserve your trip in minutes from anywhere.</p>
        </div>
      </section>
    </div>
  );
}