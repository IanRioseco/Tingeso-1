// Formatea un número de pesos con el formato de moneda local.
export const formatPesos = (valor) => {
  if (valor === null || valor === undefined || valor === '') return '$0';
  // Formatea un número de pesos con el formato de moneda local.
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: 'CLP',
    minimumFractionDigits: 0,
  }).format(valor);
};