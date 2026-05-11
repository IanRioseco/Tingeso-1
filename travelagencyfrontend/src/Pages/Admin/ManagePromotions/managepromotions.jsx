import { useState, useEffect } from 'react';
import promotionService from '../../../Services/promotionService';
import './managepromotions.css';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus, faEdit, faPowerOff, faCheck } from '@fortawesome/free-solid-svg-icons';

const EMPTY_PROMO = {
  name: '',
  discountPct: '',
  validFrom: '',
  validTo: '',
  active: true
};

export default function ManagePromotions() {
  const [promotions, setPromotions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_PROMO);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => { fetchPromotions(); }, []);

  const fetchPromotions = async () => {
    setLoading(true);
    try {
      const res = await promotionService.getAll();
      setPromotions(res.data);
    } catch (err) {
      setError('Error al obtener promociones');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm({
      ...form,
      [name]: type === 'checkbox' ? checked : value
    });
  };

  const handleNew = () => {
    setEditingId(null);
    setForm(EMPTY_PROMO);
    setShowForm(true);
    setError('');
    setSuccess('');
  };

  const handleEdit = (promo) => {
    setEditingId(promo.id);
    setForm({
      name: promo.name,
      discountPct: promo.discountPct,
      validFrom: promo.validFrom,
      validTo: promo.validTo,
      active: promo.active
    });
    setShowForm(true);
    setError('');
    setSuccess('');
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditingId(null);
    setForm(EMPTY_PROMO);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    try {
      if (editingId) {
        await promotionService.update(editingId, form);
        setSuccess('Promoción actualizada exitosamente');
      } else {
        await promotionService.create(form);
        setSuccess('Promoción creada exitosamente');
      }
      setShowForm(false);
      fetchPromotions();
    } catch (err) {
      setError(err.response?.data?.error || err.response?.data?.message || 'Error al guardar la promoción');
    }
  };

  const handleToggleStatus = async (id, currentStatus) => {
    try {
      await promotionService.changeStatus(id, !currentStatus);
      fetchPromotions();
    } catch (err) {
      setError('Error al cambiar el estado de la promoción');
    }
  };

  if (loading) {
    return <div className="container" style={{ padding: '4rem' }}>Cargando promociones...</div>;
  }

  return (
    <div className="manage-promo container">
      <div className="manage-promo-header">
        <h1>Gestión de Promociones</h1>
        {!showForm && (
          <button className="btn btn-primary" onClick={handleNew}>
            <FontAwesomeIcon icon={faPlus} /> Nueva Promoción
          </button>
        )}
      </div>

      {error && <div className="alert alert-danger">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {showForm ? (
        <div className="admin-form-card">
          <h3>{editingId ? 'Editar Promoción' : 'Crear Promoción'}</h3>
          <form className="admin-form" onSubmit={handleSubmit}>
            <label>
              Nombre comercial
              <input type="text" name="name" value={form.name} onChange={handleChange} required />
            </label>
            <label>
              Porcentaje de Descuento (%)
              <input type="number" step="0.01" min="0.01" max="100" name="discountPct" value={form.discountPct} onChange={handleChange} required />
            </label>
            <div className="form-row">
              <label>
                Válido Desde
                <input type="date" name="validFrom" value={form.validFrom} onChange={handleChange} required />
              </label>
              <label>
                Válido Hasta
                <input type="date" name="validTo" value={form.validTo} onChange={handleChange} required />
              </label>
            </div>
            <label className="checkbox-wrap">
              <input type="checkbox" name="active" checked={form.active} onChange={handleChange} />
              <span>Activa inmediatamente</span>
            </label>
            <div className="form-actions">
              <button type="button" className="btn btn-secondary" onClick={handleCancel}>Cancelar</button>
              <button type="submit" className="btn btn-primary">Guardar</button>
            </div>
          </form>
        </div>
      ) : (
        <div className="tbl-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nombre</th>
                <th>Descuento</th>
                <th>Período</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {promotions.length === 0 ? (
                <tr><td colSpan="6" style={{textAlign: 'center'}}>No hay promociones registradas</td></tr>
              ) : promotions.map((p) => (
                <tr key={p.id}>
                  <td>{p.id}</td>
                  <td>{p.name}</td>
                  <td><b>{p.discountPct}%</b></td>
                  <td className="date-col">{p.validFrom} al {p.validTo}</td>
                  <td>
                    <span className={`promo-badge ${p.active ? 'promo-active' : 'promo-inactive'}`}>
                      {p.active ? 'Activa' : 'Inactiva'}
                    </span>
                  </td>
                  <td className="actions-col">
                    <button className="icon-btn edit-btn" title="Editar" onClick={() => handleEdit(p)}>
                      <FontAwesomeIcon icon={faEdit} />
                    </button>
                    <button 
                      className={`icon-btn ${p.active ? 'off-btn' : 'on-btn'}`} 
                      title={p.active ? 'Desactivar' : 'Activar'}
                      onClick={() => handleToggleStatus(p.id, p.active)}>
                      <FontAwesomeIcon icon={p.active ? faPowerOff : faCheck} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}