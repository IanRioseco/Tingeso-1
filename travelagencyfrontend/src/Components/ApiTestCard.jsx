import './ApiTestCard.css';
import { useState } from 'react';

function ApiTestCard({ title, buttonLabel, onRequest }) {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState('Sin ejecutar');

  const handleClick = async () => {
    setLoading(true);
    setResult('Cargando...');

    try {
      const message = await onRequest();
      setResult(message || 'OK');
    } catch (error) {
      setResult(error.message || 'Error en la solicitud');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="api-card">
      <h3>{title}</h3>
      <button type="button" onClick={handleClick} disabled={loading}>
        {loading ? 'Procesando...' : buttonLabel}
      </button>
      <p className="api-result">{result}</p>
    </section>
  );
}

export default ApiTestCard;
