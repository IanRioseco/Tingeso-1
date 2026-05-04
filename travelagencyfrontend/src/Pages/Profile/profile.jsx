import { useEffect, useMemo, useState } from 'react';
import userService from '../../Services/UserService';
import { formatDateTimeCL } from '../../Utils/dateFormat';
import {
	EMPTY_PROFILE_FORM,
	formToProfilePayload,
	isAccountActive,
	profileToFormModel,
	validateProfileForm,
} from '../../Utils/profileForm';
import './profile.css';

// Función para la página de perfil de usuario
function ProfilePage() {
	const [profile, setProfile] = useState(null);
	const [form, setForm] = useState(EMPTY_PROFILE_FORM);
	const [editing, setEditing] = useState(false);
	const [loading, setLoading] = useState(true);
	const [saving, setSaving] = useState(false);
	const [error, setError] = useState('');
	const [success, setSuccess] = useState('');

    // Validación de la cuenta de usuario
	const accountIsActive = useMemo(() => {
		return isAccountActive(profile);
	}, [profile]);

    // Carga de la información de la cuenta de usuario
	useEffect(() => {
		let alive = true;
        // Carga de la información de la cuenta de usuario
		const loadProfile = async () => {
			try {
				setLoading(true);
				setError('');
				setSuccess('');

                // Carga de la información de la cuenta de usuario
				const response = await userService.me();
				if (!alive) return;

                // inicialización de los datos de la cuenta de usuario en el formulario
				const data = response.data;
				setProfile(data);
				setForm(profileToFormModel(data));
			} catch {
				if (alive) {
					setError('No se pudo cargar tu perfil. Intenta nuevamente.');
				}
			} finally {
				if (alive) {
					setLoading(false);
				}
			}
		};
		loadProfile();
		return () => {
			alive = false;
		};
	}, []);

    // Validación de los datos de la cuenta de usuario
    // Actualización de los datos de la cuenta de usuario
	const handleChange = (event) => {
		const { name, value } = event.target;
		setForm((prev) => ({ ...prev, [name]: value }));
	};

    // Cancelación de la edición de los datos de la cuenta de usuario
	const handleCancel = () => {
		if (!profile) return;
		setForm(profileToFormModel(profile));
		setError('');
		setSuccess('');
		setEditing(false);
	};

    // Guarda de los datos de la cuenta de usuario
	const handleSubmit = async (event) => {
		event.preventDefault();
        // Validación de los datos de la cuenta de usuario
		const validationError = validateProfileForm(form);
		if (validationError) {
			setError(validationError);
			setSuccess('');
			return;
		}
		// Datos de la cuenta de usuario a actualizar
		try {
			setSaving(true);
			setError('');
			setSuccess('');
            // Datos de la cuenta de usuario a actualizar
			const payload = formToProfilePayload(form);
            // Actualización de la información de la cuenta de usuario
			const response = await userService.updateMe(payload);
			const updated = response.data;
			// Alerta de éxito y actualización de los datos de la cuenta de usuario en el estado y el formulario
			setProfile(updated);
			setForm(profileToFormModel(updated));
			setEditing(false);
			setSuccess('Perfil actualizado correctamente.');
		} catch {
			setError('No se pudo guardar el perfil. Verifica los datos e intenta nuevamente.');
		} finally {
			setSaving(false);
		}
	};

	if (loading) {
		return (
			<section className="profile-page container">
				<p>Cargando perfil...</p>
			</section>
		);
	}

	return (
		<section className="profile-page container">
            {/* header de la página de perfil de usuario */}
			<header className="profile-header">
				<h1>Mi Perfil</h1>
				<p>Gestiona tu informacion personal y revisa el estado de tu cuenta.</p>
			</header>

            {/* divs para las alertas de error y éxito */}
			{error && <div className="profile-alert profile-alert-error">{error}</div>}
			{success && <div className="profile-alert profile-alert-success">{success}</div>}

			{profile && (
				<div className="profile-grid">
                    {/* tarjeta para la información de cuenta */}
					<article className="profile-card">
                        {/* div para el título de la tarjeta */}
						<div className="profile-card-top">
							<h2>Informacion de cuenta</h2>
							<div className="profile-badges">
								<span className={`badge-status ${accountIsActive ? 'active' : 'inactive'}`}>
									{accountIsActive ? 'Activa' : 'Inactiva'}
								</span>
								<span className="badge-role">Rol: {profile.role}</span>
							</div>
						</div>
                        {/* lista de datos de la cuenta */}
						<div className="profile-readonly-list">
							<div>
								<span>Email</span>
								<strong>{profile.email || '-'}</strong>
							</div>
							<div>
								<span>Estado interno</span>
								<strong>{profile.status || '-'}</strong>
							</div>
							<div>
								<span>Fecha de alta</span>
								<strong>{formatDateTimeCL(profile.createdAt)}</strong>
							</div>
						</div>

						{!accountIsActive && (
							<p className="profile-warning">
								Tu cuenta no se encuentra activa. Si necesitas ayuda, contacta a un administrador.
							</p>
						)}
					</article>

                    {/* tarjeta para los datos personales */}
					<article className="profile-card">
                        {/* div para el título de la tarjeta */}
						<div className="profile-card-top">
							<h2>Datos personales</h2>
							{!editing && (
								<button 
                                    type="button" 
                                    className="btn 
                                    btn-primary" 
                                    onClick={() => {setError(''); setSuccess(''); setEditing(true);}} 
                                    disabled={!accountIsActive}
                                    >
									Editar
								</button>
							)}
						</div>

                        {/* formulario para editar los datos personales */}
						<form className="profile-form" onSubmit={handleSubmit}>
                            {/* label para el campo de nombre completo */}
							<label htmlFor="fullName">
								Nombre completo *
								<input 
                                    id="fullName" 
                                    name="fullName" 
                                    type="text" 
                                    value={form.fullName} 
                                    onChange={handleChange} 
                                    disabled={!editing || saving} 
                                    maxLength={120} 
                                    />
							</label>
                            {/* label para el campo de teléfono */}
							<label htmlFor="phone">
								Telefono
								<input 
                                    id="phone" 
                                    name="phone" 
                                    type="text" value={form.phone} 
                                    onChange={handleChange} 
                                    disabled={!editing || saving} 
                                    maxLength={20} 
                                    />   
							</label>
                            {/* label para el campo de la documento de identidad */}
							<label htmlFor="documentId">
								Documento de identidad
								<input 
                                    id="documentId" 
                                    name="documentId" 
                                    type="text" 
                                    value={form.documentId} 
                                    onChange={handleChange} 
                                    disabled={!editing || saving} 
                                    maxLength={30} 
                                    />
							</label>
                            {/* label para el campo de la nacionalidad */}
							<label htmlFor="nationality">
								Nacionalidad
								<input 
                                    id="nationality" 
                                    name="nationality" 
                                    type="text" value={form.nationality}   
                                    onChange={handleChange} 
                                    disabled={!editing || saving} maxLength={60} 
                                    />
							</label>

							{editing && (
                                {/* div para las acciones de guardar y cancelar */},
								<div className="profile-actions">
									<button 
                                        type="submit" 
                                        className="btn 
                                        btn-primary" 
                                        disabled={saving}
                                        >
										{saving ? 'Guardando...' : 'Guardar cambios'}
									</button>
									<button 
                                        type="button" 
                                        className="btn 
                                        btn-secondary" 
                                        onClick={handleCancel} 
                                        disabled={saving}
                                        >
										Cancelar
									</button>
								</div>
							)}
						</form>
					</article>
				</div>
			)}
		</section>
	);
}

export default ProfilePage;