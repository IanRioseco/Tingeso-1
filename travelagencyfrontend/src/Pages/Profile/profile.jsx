import { useEffect, useMemo, useState } from 'react';
import userService from '../../Services/UserService';
import './profile.css';

const emptyForm = {
	fullName: '',
	phone: '',
	documentId: '',
	nationality: '',
};

function ProfilePage() {
	const [profile, setProfile] = useState(null);
	const [form, setForm] = useState(emptyForm);
	const [editing, setEditing] = useState(false);
	const [loading, setLoading] = useState(true);
	const [saving, setSaving] = useState(false);
	const [error, setError] = useState('');
	const [success, setSuccess] = useState('');

	const accountIsActive = useMemo(() => {
		if (!profile) return true;
		const status = String(profile.status || '').toUpperCase();
		return Boolean(profile.active) && status === 'ACTIVE';
	}, [profile]);

	useEffect(() => {
		let alive = true;

		const loadProfile = async () => {
			try {
				setLoading(true);
				setError('');
				setSuccess('');

				const response = await userService.me();
				if (!alive) return;

				const data = response.data;
				setProfile(data);
				setForm({
					fullName: data.fullName || '',
					phone: data.phone || '',
					documentId: data.documentId || '',
					nationality: data.nationality || '',
				});
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

	const validate = () => {
		const trimmedName = form.fullName.trim();
		if (!trimmedName) {
			return 'El nombre completo es obligatorio.';
		}
		if (trimmedName.length < 3) {
			return 'El nombre completo debe tener al menos 3 caracteres.';
		}

		const trimmedPhone = form.phone.trim();
		if (trimmedPhone && !/^\+?[0-9\s()-]{7,20}$/.test(trimmedPhone)) {
			return 'El telefono tiene un formato invalido.';
		}

		if (form.documentId.trim().length > 30) {
			return 'El documento no puede superar 30 caracteres.';
		}

		if (form.nationality.trim().length > 60) {
			return 'La nacionalidad no puede superar 60 caracteres.';
		}

		return '';
	};

	const handleChange = (event) => {
		const { name, value } = event.target;
		setForm((prev) => ({ ...prev, [name]: value }));
	};

	const handleCancel = () => {
		if (!profile) return;
		setForm({
			fullName: profile.fullName || '',
			phone: profile.phone || '',
			documentId: profile.documentId || '',
			nationality: profile.nationality || '',
		});
		setError('');
		setSuccess('');
		setEditing(false);
	};

	const handleSubmit = async (event) => {
		event.preventDefault();
		const validationError = validate();
		if (validationError) {
			setError(validationError);
			setSuccess('');
			return;
		}

		try {
			setSaving(true);
			setError('');
			setSuccess('');

			const payload = {
				fullName: form.fullName.trim(),
				phone: form.phone.trim(),
				documentId: form.documentId.trim(),
				nationality: form.nationality.trim(),
			};

			const response = await userService.updateMe(payload);
			const updated = response.data;

			setProfile(updated);
			setForm({
				fullName: updated.fullName || '',
				phone: updated.phone || '',
				documentId: updated.documentId || '',
				nationality: updated.nationality || '',
			});
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
			<header className="profile-header">
				<h1>Mi Perfil</h1>
				<p>Gestiona tu informacion personal y revisa el estado de tu cuenta.</p>
			</header>

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
								<strong>{profile.createdAt ? new Date(profile.createdAt).toLocaleString() : '-'}</strong>
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
									className="btn btn-primary"
									onClick={() => {
										setError('');
										setSuccess('');
										setEditing(true);
									}}
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
								<input id="fullName" name="fullName" type="text" value={form.fullName} onChange={handleChange} disabled={!editing || saving} maxLength={120} />
							</label>
                            {/* label para el campo de teléfono */}
							<label htmlFor="phone">
								Telefono
								<input id="phone" name="phone" type="text" value={form.phone} onChange={handleChange} disabled={!editing || saving} maxLength={20} />   
							</label>
                            {/* label para el campo de la documento de identidad */}
							<label htmlFor="documentId">
								Documento de identidad
								<input id="documentId" name="documentId" type="text" value={form.documentId} onChange={handleChange} disabled={!editing || saving} maxLength={30} />
							</label>
                            {/* label para el campo de la nacionalidad */}
							<label htmlFor="nationality">
								Nacionalidad
								<input id="nationality" name="nationality" type="text" value={form.nationality} onChange={handleChange} disabled={!editing || saving} maxLength={60} />
							</label>

							{editing && (
                                {/* div para las acciones de guardar y cancelar */},
								<div className="profile-actions">
									<button type="submit" className="btn btn-primary" disabled={saving}>
										{saving ? 'Guardando...' : 'Guardar cambios'}
									</button>
									<button type="button" className="btn btn-secondary" onClick={handleCancel} disabled={saving}>
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
