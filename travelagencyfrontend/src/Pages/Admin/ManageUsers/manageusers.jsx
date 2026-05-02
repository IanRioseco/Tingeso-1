import { useState, useEffect } from 'react';
import userService from '../../../Services/UserService';
import { EMPTY_USER_FORM, userToFormModel } from '../../../Utils/userForm';
import './manageusers.css';

export default function ManageUsers() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [editing, setEditing] = useState(null);
    const [form, setForm] = useState(EMPTY_USER_FORM);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => { fetchUsers(); }, []);

    const fetchUsers = async () => {
        setLoading(true);
        try {
            const res = await userService.getAll();
            setUsers(res.data);
        } catch (err) {
            setError('Error al cargar los usuarios');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleEdit = (user) => {
        setEditing(user);
        setForm(userToFormModel(user));
        setShowForm(true);
        setError('');
        setSuccess('');
    };

    const handleCancel = () => {
        setShowForm(false);
        setEditing(null);
        setForm(EMPTY_USER_FORM);
        setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        try {
            await userService.update(editing.id, form);
            setSuccess('Usuario actualizado correctamente');
            fetchUsers();
            handleCancel();
        } catch (err) {
            setError('Error al guardar el usuario. Por favor, revisa los datos e intenta nuevamente.');
        }
    };

    const handleDeactivate = async (id) => {
        if (!window.confirm('¿Estás seguro de que deseas desactivar este usuario? Esta acción no se puede deshacer.'))
            return;
        try {
            await userService.deactivate(id);
            setSuccess('Usuario desactivado correctamente');
            fetchUsers();
        } catch (err) {
            setError('Error al desactivar el usuario. Por favor, intenta nuevamente.');
        }
    };

    return (
        <div className="manage-users container">
            <div className="manage-users-header">
                <h1>Gestión de Usuarios</h1>
            </div>
            {success && <div className="alert alert-success">{success}</div>}
            {error && <div className="alert alert-danger">{error}</div>}

            {/* Formulario para editar usuario */}
            {showForm && (
                <div className="manage-users-form">
                    <h3>Editar usuario</h3>
                    <form onSubmit={handleSubmit} className='manage-users-form-form'>
                        <div className="form-group">
                            <div className="form-group-label">
                                <label>Nombre Completo</label>
                                <input
                                    type="text"
                                    name="fullName"
                                    value={form.fullName || ''}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                            <div className="form-group-label">
                                <label>Teléfono</label>
                                <input
                                    type="tel"
                                    name="phone"
                                    value={form.phone || ''}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>
                        <div className="form-group">
                            <div className="form-group-label">
                                <label>Documento de Identidad</label>
                                <input
                                    type="text"
                                    name="documentId"
                                    value={form.documentId || ''}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                            <div className="form-group-label">
                                <label>Nacionalidad</label>
                                <input
                                    type="text"
                                    name="nationality"
                                    value={form.nationality || ''}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>

                        <div className='manage-users-form-form-actions'>
                            <button type="submit" className="btn btn-primary">
                                Actualizar
                            </button>
                            <button type="button" className="btn btn-secondary" onClick={handleCancel}>
                                Cancelar
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* Tabla de usuarios */}
            {loading && <p>Cargando usuarios...</p>}
            {!loading && users.length === 0 && <p>No se encontraron usuarios.</p>}
            {!loading && users.length > 0 && (
                <table className="manage-users-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Email</th>
                            <th>Nombre Completo</th>
                            <th>Teléfono</th>
                            <th>Documento</th>
                            <th>Nacionalidad</th>
                            <th>Estado</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((user) => (
                            <tr key={user.id}>
                                <td>{user.id}</td>
                                <td>{user.email}</td>
                                <td>{user.fullName || '-'}</td>
                                <td>{user.phone || '-'}</td>
                                <td>{user.documentId || '-'}</td>
                                <td>{user.nationality || '-'}</td>
                                <td>
                                    <span className={`status-badge ${user.status?.toLowerCase() || 'active'}`}>
                                        {user.status || 'Activo'}
                                    </span>
                                </td>
                                <td>
                                    <button
                                        className="btn btn-primary"
                                        onClick={() => handleEdit(user)}
                                        disabled={user.status === 'INACTIVE' || user.status === 'Inactivo'}
                                    >
                                        Editar
                                    </button>
                                    <button
                                        className="btn btn-danger"
                                        onClick={() => handleDeactivate(user.id)}
                                        disabled={user.status === 'INACTIVE' || user.status === 'Inactivo'}
                                    >
                                        Desactivar
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}
