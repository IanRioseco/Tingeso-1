// Formatea una fecha con el formato de fecha local.
export const formatDateCL = (dateString) => {
  if (!dateString) {
    return '-';
  }
  // Formatea una fecha con el formato de fecha local.
  return new Date(dateString).toLocaleDateString('es-CL');
};
// Formatea una fecha y hora con el formato de fecha y hora local.
export const formatDateTimeCL = (dateString) => {
  if (!dateString) {
    return '-';
  }
  // Formatea una fecha y hora con el formato de fecha y hora local.
  return new Date(dateString).toLocaleString('es-CL');
};
