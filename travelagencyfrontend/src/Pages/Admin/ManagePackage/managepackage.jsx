import {useState, useEffect} from 'react';
import packageService from '../../../Services/packageService';
import { formatPesos } from '../../../Utils/currency';
import { getAvailableSlots } from '../../../Utils/packageFilters';
import { EMPTY_PACKAGE_FORM, formToPackagePayload, packageToFormModel } from '../../../Utils/packageForm';
import './managepackage.css';

export default function ManagePackage() {
    const [packages, setPackages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [editing, setEditing] = useState(null);
    const [form, setForm] = useState(EMPTY_PACKAGE_FORM);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => {fetchPackages()}, []);

    const fetchPackages = async () => {
        setLoading(true);
        try {
            const res = await packageService.getAll();
            setPackages(res.data);
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (e) => {
        setForm({...form, [e.target.name]: e.target.value});
    };

    const handleEdit = (pkg) => {
        setEditing(pkg);
        setForm(packageToFormModel(pkg));
        setShowForm(true);
        setError('');
        setSuccess('');
    };
    const handleNew = () => {
        setEditing(null);
        setForm(EMPTY_PACKAGE_FORM);
        setShowForm(true);
        setError('');
        setSuccess('');
    };

    const handleCancel = () => {
        setShowForm(false);
        setEditing(null);
        setForm(EMPTY_PACKAGE_FORM);
        setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        try {
            const processedForm = formToPackagePayload(form);
            
            if (editing) {
                await packageService.update(editing.id, processedForm);
                setSuccess('Paquete actualizado correctamente');
            } else {
                await packageService.create(processedForm);
                setSuccess('Paquete creado correctamente');
            }
            fetchPackages();
            handleCancel();
            } catch (err) {
                setError('Error al guardar el paquete. Por favor, revisa los datos e intenta nuevamente.');
        }
    };

    const handleChangeStatus = async (id, status) => {
        try {
            await packageService.changeStatus(id, status);
            fetchPackages();
        } catch (err) {
            setError('Error al cambiar el estado del paquete. Por favor, revisa los datos e intenta nuevamente.');
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('¿Estás seguro de que deseas eliminar este paquete? Esta acción no se puede deshacer.')) 
            return;
        try {
            await packageService.delete(id);
            fetchPackages();
        } catch (err) {
            setError('Error al eliminar el paquete. Por favor, revisa los datos e intenta nuevamente.');    
        }
    };

    return (
        <div className="manage-package container">
            <div className="manage-package-header">
                <h1>Control de paquetes</h1>
                <button className="btn btn-primary" onClick={handleNew}>Nuevo paquete</button>
            </div>
            {success && <div className="alert alert-success">{success}</div>}
            {error && <div className="alert alert-danger">{error}</div>}

            {/* formulario para crear o editar un paquete */}
            {showForm && (
                <div className="manage-package-form">
                    <h3>{editing ? 'Editar paquete' : 'Crear nuevo paquete'}</h3>
                    <form onSubmit={handleSubmit} className='manage-package-form-form'>
                        {/* campos para el formulario de creación de paquetes */}

                        {/* div para los campos de nombre y destino del paquete */}
                        <div className="form-group">
                            <div className="form-group-label">
                                <label> Nombre </label>
                                <input type="text" name="name" value={form.name || ''} onChange={handleChange} required />
                            </div>
                            <div className="form-group-label">
                                <label> Destino </label>
                                <input type="text" name="destination" value={form.destination || ''} onChange={handleChange} required />
                            </div>
                        </div>
                        {/* div para el campo de descripción del paquete */}
                        <div className="form-group">    
                            <label> Descripción </label>
                            <textarea name="description" value={form.description || ''} onChange={handleChange} required></textarea>
                        </div>
                        {/* div para los campos de fechas, precio, cupos disponibles del paquete */}
                        <div className="form-group">    
                            <div className="form-group-label">
                                <label> Fecha de inicio </label>
                                <input type="date" name="startDate" value={form.startDate || ''} onChange={handleChange} required />
                            </div>
                            <div className="form-group-label">
                                <label> Fecha de fin </label>
                                <input type="date" name="endDate" value={form.endDate || ''} onChange={handleChange} required />
                            </div>
                            <div className="form-group-label">
                                <label> Precio (CLP) </label>
                                <input type="text" name="price" value={form.price || ''} onChange={handleChange} placeholder="Ej: 230.000 o 230000" required />
                            </div>
                            <div className="form-group-label">
                                <label> Cupos disponibles (cantidad) </label>
                                <input type="number" name="totalSlots" value={form.totalSlots || ''} onChange={handleChange} required />
                            </div>
                            <div className="form-group-label">
                                <label> Días de duración </label>
                                <input type="number" name="durationDays" value={form.durationDays || ''} onChange={handleChange} />
                            </div>
                        </div>
                        {/** div para los campos de tipo de viaje y temporada del paquete */}
                        <div className="form-group">
                            <div className="form-group-label">
                                <label> Tipo de viaje </label>
                                <input type="text" name="travelType" value={form.travelType || ''} onChange={handleChange} required />
                            </div>
                            <div className="form-group-label">
                                <label> Temporada </label>
                                <input type="text" name="season" value={form.season || ''} onChange={handleChange} required />
                            </div>
                        </div>
                        {/* div para los campos de restricciones del paquete */}
                        <div className="form-group">
                            <label> Restricciones </label>
                            <textarea name="restrictions" value={form.restrictions || ''} onChange={handleChange} required></textarea>
                        </div>
                        {/* div para los campos de servicios incluidos del paquete */}
                        <div className="form-group">
                            <label> Servicios incluidos </label>
                            <textarea name="servicesIncluded" value={form.servicesIncluded || ''} onChange={handleChange} required></textarea>
                        </div>
                        {/* div para los campos de condiciones del paquete */}
                        <div className="form-group">    
                            <label> Condiciones </label>
                            <textarea name="conditions" value={form.conditions || ''} onChange={handleChange} required></textarea>
                        </div>

                        <div className='manage-package-form-form-actions'> 
                            <button type="submit" className="btn btn-primary">
                                {editing ? 'Actualizar' : 'Crear'}
                            </button>
                            <button type="button" className="btn btn-secondary" onClick={handleCancel}>
                                Cancelar 
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* tabla para mostrar los paquetes existentes */}
            {loading && <p>Cargando paquetes...</p>}
            {!loading && packages.length === 0 && <p>No se encontraron paquetes.</p>}
            {!loading && packages.length > 0 && (
                <table className="manage-package-table">
                    <thead>
                        <tr>
                            <th>Nombre</th>
                            <th>Destino</th>
                            <th>F/inicio</th>
                            <th>F/fin</th>
                            <th>Precio</th>
                            <th>Cupos</th>
                            <th>Tipo</th>
                            <th>Temporada</th>
                            <th>Estado</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        {packages.map((pkg) => (
                            <tr key={pkg.id}>    
                                <td>{pkg.name}</td>
                                <td>{pkg.destination}</td>
                                <td>{pkg.startDate}</td>
                                <td>{pkg.endDate}</td>
                                <td>{formatPesos(pkg.price)}</td>
                                <td>{getAvailableSlots(pkg)}</td>
                                <td>{pkg.travelType}</td>
                                <td>{pkg.season}</td>
                                <td>{pkg.status}</td>
                                <td>
                                    <button className="btn btn-primary" onClick={() => handleEdit(pkg)}>Editar</button>
                                    <button className="btn btn-danger" onClick={() => handleDelete(pkg.id)}>Eliminar</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}
    
    
