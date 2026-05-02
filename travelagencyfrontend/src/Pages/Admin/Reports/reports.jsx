import { useState } from 'react';
import reportService from '../../../Services/reportService';
import { formatPesos } from '../../../Utils/currency';
import { formatDateCL } from '../../../Utils/dateFormat';
import './reports.css';

export default function ReportsPage() {
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [sales, setSales] = useState([]);
  const [ranking, setRanking] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      setLoading(true);
      setError('');
      const [salesResponse, rankingResponse] = await Promise.all([
        reportService.getSales(from, to),
        reportService.getRanking(from, to),
      ]);
      setSales(salesResponse.data || []);
      setRanking(rankingResponse.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudieron generar los reportes.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="admin-reports container">
      <div className="admin-reports-header">
        <div>
          <p className="admin-reports-kicker">Reportería</p>
          <h1>Reportes administrativos</h1>
        </div>
      </div>

      <form className="reports-filter" onSubmit={handleSubmit}>
        <label>
          Fecha inicio
          <input type="date" value={from} onChange={(event) => setFrom(event.target.value)} required />
        </label>
        <label>
          Fecha término
          <input type="date" value={to} onChange={(event) => setTo(event.target.value)} required />
        </label>
        <button className="btn btn-primary" type="submit" disabled={loading}>
          {loading ? 'Generando...' : 'Generar reportes'}
        </button>
      </form>

      {error && <div className="alert alert-danger">{error}</div>}

      <section className="report-card">
        <h3>Ventas por período</h3>
        <div className="report-table-wrap">
          <table className="report-table">
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Cliente</th>
                <th>Email</th>
                <th>Paquete</th>
                <th>Pasajeros</th>
                <th>Total</th>
                <th>Pagado</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {sales.length === 0 ? (
                <tr><td colSpan="8" className="empty-row">Sin datos para mostrar</td></tr>
              ) : sales.map((item) => (
                <tr key={`${item.bookingId}-${item.paymentId || 'booking'}`}>
                  <td>{formatDateCL(item.operationDate)}</td>
                  <td>{item.clientName}</td>
                  <td>{item.clientEmail}</td>
                  <td>{item.packageName}</td>
                  <td>{item.passengers}</td>
                  <td>{formatPesos(item.bookingTotal)}</td>
                  <td>{formatPesos(item.amountPaid)}</td>
                  <td>{item.bookingStatus}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="report-card">
        <h3>Ranking de paquetes vendidos</h3>
        <div className="report-table-wrap">
          <table className="report-table">
            <thead>
              <tr>
                <th>Paquete</th>
                <th>Reservas</th>
                <th>Pasajeros</th>
                <th>Ingresos</th>
              </tr>
            </thead>
            <tbody>
              {ranking.length === 0 ? (
                <tr><td colSpan="4" className="empty-row">Sin datos para mostrar</td></tr>
              ) : ranking.map((item) => (
                <tr key={item.packageId}>
                  <td>{item.packageName}</td>
                  <td>{item.reservationsCount}</td>
                  <td>{item.passengersCount}</td>
                  <td>{formatPesos(item.totalRevenue)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
