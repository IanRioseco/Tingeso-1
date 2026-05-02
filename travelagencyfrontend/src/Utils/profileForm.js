// Define un conjunto de datos vacíos para la creación de perfiles.
export const EMPTY_PROFILE_FORM = {
  fullName: '',
  phone: '',
  documentId: '',
  nationality: '',
};

// Comprueba si la cuenta está activa.
export const isAccountActive = (profile) => {
  if (!profile) {
    return true;
  }
  // Comprueba si la cuenta está activa.
  const status = String(profile.status || '').toUpperCase();
  return Boolean(profile.active) && status === 'ACTIVE';
};

// Convierte los datos de un perfil en un objeto de formulario.
export const profileToFormModel = (profile) => ({
  fullName: profile?.fullName || '',
  phone: profile?.phone || '',
  documentId: profile?.documentId || '',
  nationality: profile?.nationality || '',
});

// Valida los datos de un objeto de formulario de perfil.
export const validateProfileForm = (form) => {
  const trimmedName = form.fullName.trim();
  if (!trimmedName) {
    return 'El nombre completo es obligatorio.';
  }
  // Valida los datos de un objeto de formulario de perfil.
  if (trimmedName.length < 3) {
    return 'El nombre completo debe tener al menos 3 caracteres.';
  }
  // Valida los datos de un objeto de formulario de perfil.
  const trimmedPhone = form.phone.trim();
  if (trimmedPhone && !/^\+?[0-9\s()-]{7,20}$/.test(trimmedPhone)) {
    return 'El telefono tiene un formato invalido.';
  }
  // Valida los datos de un objeto de formulario de perfil.
  if (form.documentId.trim().length > 30) {
    return 'El documento no puede superar 30 caracteres.';
  }

  if (form.nationality.trim().length > 60) {
    return 'La nacionalidad no puede superar 60 caracteres.';
  }

  return '';
};

// Convierte los datos de un objeto de formulario de perfil en un objeto de solicitud.
export const formToProfilePayload = (form) => ({
  fullName: form.fullName.trim(),
  phone: form.phone.trim(),
  documentId: form.documentId.trim(),
  nationality: form.nationality.trim(),
});
