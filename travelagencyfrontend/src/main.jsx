import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import ApiTestCard from './Components/ApiTestCard.jsx';
import reportWebVitals from './reportWebVitals';

function FrontSmokeTest() {
  const testUsersEndpoint = async () => {
    const response = await fetch('/api/users');
    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || 'No se pudo consultar /api/users');
    }

    return `Conexion OK. Usuarios recibidos: ${Array.isArray(data) ? data.length : 0}`;
  };

  return (
    <main className="test-page">
      <h1>Frontend de prueba</h1>
      <p>Prueba rapida para validar backend y frontend.</p>
      <ApiTestCard
        title="Probar GET /api/users"
        buttonLabel="Ejecutar prueba"
        onRequest={testUsersEndpoint}
      />
    </main>
  );
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <FrontSmokeTest />
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
