// Define un conjunto de datos vacíos para la gestión de usuarios.
export const EMPTY_USER_FORM = {
  fullName: '',
  phone: '',
  documentId: '',
  nationality: ''
};

// Convierte los datos de un usuario en un objeto de formulario.
export const userToFormModel = (user) => ({
  fullName: user?.fullName || '',
  phone: user?.phone || '',
  documentId: user?.documentId || '',
  nationality: user?.nationality || ''
});

// Validaciones básicas de usuario
export const validateUserForm = (form) => {
  const errors = [];
  if (!form.fullName?.trim()) errors.push('El nombre completo es requerido');
  if (!form.phone?.trim()) errors.push('El teléfono es requerido');
  if (!form.documentId?.trim()) errors.push('El documento de identidad es requerido');
  if (!form.nationality?.trim()) errors.push('La nacionalidad es requerida');
  return errors;
};
